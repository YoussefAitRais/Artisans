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

@Service
public class ArtisanService {

    private final ArtisanRepository artisanRepository;


    public ArtisanService(ArtisanRepository artisanRepository) {
        this.artisanRepository = artisanRepository;
    }

    // ============ PUBLIC OPERATIONS ============
    

    @Transactional(readOnly = true)
    public Page<ArtisanResponse> searchArtisans(String metier, String localisation, String q, Pageable pageable) {
        Specification<Artisan> searchCriteria = buildSearchSpecification(metier, localisation, q);
        return artisanRepository.findAll(searchCriteria, pageable)
                .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public ArtisanResponse getArtisanById(Long id) {
        Artisan artisan = findArtisanById(id);
        return mapToResponse(artisan);
    }

    // ============ ARTISAN SELF-SERVICE OPERATIONS ============
    

    @Transactional(readOnly = true)
    public ArtisanResponse getCurrentArtisanProfile(Utilisateur currentUser) {
        Artisan artisan = findArtisanById(currentUser.getId());
        return mapToResponse(artisan);
    }


    @Transactional
    public ArtisanResponse updateCurrentArtisanProfile(Utilisateur currentUser, ArtisanUpdateRequest updateRequest) {
        Artisan artisan = findArtisanById(currentUser.getId());
        
        updateArtisanFields(artisan, updateRequest);
        
        artisanRepository.save(artisan);
        return mapToResponse(artisan);
    }

    // ============ ADMINISTRATIVE OPERATIONS ============
    

    @Transactional(readOnly = true)
    public Page<ArtisanResponse> getAllArtisansForAdmin(Pageable pageable) {
        return artisanRepository.findAll(pageable)
                .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public ArtisanResponse getArtisanForAdmin(Long id) {
        Artisan artisan = findArtisanById(id);
        return mapToResponse(artisan);
    }


    @Transactional
    public ArtisanResponse updateArtisanAsAdmin(Long id, ArtisanUpdateRequest updateRequest) {
        Artisan artisan = findArtisanById(id);
        
        updateArtisanFields(artisan, updateRequest);
        
        artisanRepository.save(artisan);
        return mapToResponse(artisan);
    }


    @Transactional
    public void deleteArtisanAsAdmin(Long id) {
        if (!artisanRepository.existsById(id)) {
            throw new IllegalArgumentException("Artisan not found with ID: " + id);
        }
        artisanRepository.deleteById(id);
    }

    // ============ PRIVATE HELPER METHODS ============
    

    private Artisan findArtisanById(Long id) {
        return artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artisan not found with ID: " + id));
    }


    private String cleanString(String input) {
        return input != null ? input.trim() : null;
    }


    private Specification<Artisan> buildSearchSpecification(String metier, String localisation, String generalQuery) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by profession if provided
            if (isNotBlank(metier)) {
                predicates.add(createLikePredicateIgnoreCase(criteriaBuilder, root.get("metier"), metier));
            }

            // Filter by location if provided
            if (isNotBlank(localisation)) {
                predicates.add(createLikePredicateIgnoreCase(criteriaBuilder, root.get("localisation"), localisation));
            }

            // General search across multiple fields if provided
            if (isNotBlank(generalQuery)) {
                predicates.add(buildGeneralSearchPredicate(criteriaBuilder, root, generalQuery));
            }

            return predicates.isEmpty()
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    private Predicate buildGeneralSearchPredicate(jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                                   jakarta.persistence.criteria.Root<Artisan> root,
                                                   String searchTerm) {
        String likePattern = "%" + searchTerm.toLowerCase().trim() + "%";

        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), likePattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("prenom")), likePattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("metier")), likePattern)
        );
    }


    private Predicate createLikePredicateIgnoreCase(jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                                     jakarta.persistence.criteria.Path<String> field,
                                                     String value) {
        String likePattern = "%" + value.toLowerCase().trim() + "%";
        return criteriaBuilder.like(criteriaBuilder.lower(field), likePattern);
    }


    private boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }


    private ArtisanResponse mapToResponse(Artisan artisan) {
        return new ArtisanResponse(
                artisan.getId(),
                artisan.getNom(),
                artisan.getPrenom(),
                artisan.getEmail(),
                artisan.getMetier(),
                artisan.getLocalisation(),
                artisan.getDescription()
        );
    }


    private void updateArtisanFields(Artisan artisan, ArtisanUpdateRequest updateRequest) {
        artisan.setNom(cleanString(updateRequest.getNom()));
        artisan.setPrenom(cleanString(updateRequest.getPrenom()));
        artisan.setMetier(cleanString(updateRequest.getMetier()));
        artisan.setLocalisation(cleanString(updateRequest.getLocalisation()));
        artisan.setDescription(cleanString(updateRequest.getDescription()));
    }
}
