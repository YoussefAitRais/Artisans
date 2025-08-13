package org.event.backend.service.artisan;

import org.event.backend.dto.artisan.ArtisanResponse;
import org.event.backend.dto.artisan.ArtisanUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Utilisateur;
import org.event.backend.repository.ArtisanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for artisan profiles: search, get, self-update, admin ops.
 */
@Service
public class ArtisanService {

    private final ArtisanRepository artisanRepository;

    public ArtisanService(ArtisanRepository artisanRepository) {
        this.artisanRepository = artisanRepository;
    }

    // ---------- Public: search with optional filters ----------
    @Transactional(readOnly = true)
    public Page<ArtisanResponse> search(String metier, String localisation, String q, Pageable pageable) {
        Specification<Artisan> spec = buildSpec(metier, localisation, q);
        return artisanRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ArtisanResponse getById(Long id) {
        Artisan a = artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artisan not found"));
        return toResponse(a);
    }

    // ---------- Self (ROLE_ARTISAN) ----------
    @Transactional(readOnly = true)
    public ArtisanResponse getMe(Utilisateur current) {
        Long id = current.getId();
        Artisan a = artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artisan profile not found"));
        return toResponse(a);
    }

    @Transactional
    public ArtisanResponse updateMe(Utilisateur current, ArtisanUpdateRequest req) {
        Long id = current.getId();
        Artisan a = artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artisan profile not found"));

        // Update allowed fields
        a.setNom(req.getNom().trim());
        a.setPrenom(req.getPrenom());
        a.setMetier(req.getMetier().trim());
        a.setLocalisation(req.getLocalisation());
        a.setDescription(req.getDescription());

        // Explicit save for clarity (managed entity would also flush on commit)
        artisanRepository.save(a);
        return toResponse(a);
    }

    // ---------- Admin ----------
    @Transactional(readOnly = true)
    public Page<ArtisanResponse> adminList(Pageable pageable) {
        return artisanRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ArtisanResponse adminGet(Long id) {
        Artisan a = artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artisan not found"));
        return toResponse(a);
    }

    @Transactional
    public ArtisanResponse adminUpdate(Long id, ArtisanUpdateRequest req) {
        Artisan a = artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artisan not found"));

        a.setNom(req.getNom().trim());
        a.setPrenom(req.getPrenom());
        a.setMetier(req.getMetier().trim());
        a.setLocalisation(req.getLocalisation());
        a.setDescription(req.getDescription());

        artisanRepository.save(a);
        return toResponse(a);
    }

    @Transactional
    public void adminDelete(Long id) {
        if (!artisanRepository.existsById(id)) {
            throw new IllegalArgumentException("Artisan not found");
        }
        artisanRepository.deleteById(id);
    }

    // ---------- Helpers ----------

    private Specification<Artisan> buildSpec(String metier, String localisation, String q) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (metier != null && !metier.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("metier")), "%" + metier.toLowerCase().trim() + "%"));
            }
            if (localisation != null && !localisation.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("localisation")), "%" + localisation.toLowerCase().trim() + "%"));
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase().trim() + "%";
                Predicate byNom = cb.like(cb.lower(root.get("nom")), like);
                Predicate byPrenom = cb.like(cb.lower(root.get("prenom")), like);
                Predicate byDesc = cb.like(cb.lower(root.get("description")), like);
                Predicate byMetier = cb.like(cb.lower(root.get("metier")), like);
                preds.add(cb.or(byNom, byPrenom, byDesc, byMetier));
            }

            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private ArtisanResponse toResponse(Artisan a) {
        return new ArtisanResponse(
                a.getId(),
                a.getNom(),
                a.getPrenom(),
                a.getEmail(),
                a.getMetier(),
                a.getLocalisation(),
                a.getDescription()
        );
    }
}
