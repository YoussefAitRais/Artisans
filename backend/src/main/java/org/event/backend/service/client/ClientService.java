package org.event.backend.service.client;

import org.event.backend.dto.artisan.ArtisanResponse;
import org.event.backend.dto.client.ClientResponse;
import org.event.backend.dto.client.ClientUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Client;
import org.event.backend.entity.Utilisateur;
import org.event.backend.repository.ArtisanRepository;
import org.event.backend.repository.ClientRepository;
import org.event.backend.repository.QuoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;


@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ArtisanRepository artisanRepository;
    private final QuoteRepository quoteRepository;
    private final EntityManager entityManager;

    public ClientService(ClientRepository clientRepository,
                        ArtisanRepository artisanRepository,
                        QuoteRepository quoteRepository,
                        EntityManager entityManager) {
        this.clientRepository = clientRepository;
        this.artisanRepository = artisanRepository;
        this.quoteRepository = quoteRepository;
        this.entityManager = entityManager;
    }

    // ---------- Self (ROLE_CLIENT) ----------

    @Transactional(readOnly = true)
    public ClientResponse getMe(Utilisateur current) {
        Long id = current.getId();
        Client c = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client profile not found"));
        return toResponse(c);
    }

    @Transactional
    public ClientResponse updateMe(Utilisateur current, ClientUpdateRequest req) {
        Long id = current.getId();
        Client c = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client profile not found"));

        c.setNom(req.getNom().trim());
        c.setPrenom(req.getPrenom());
        c.setTelephone(req.getTelephone());

        clientRepository.save(c);
        return toResponse(c);
    }

    // Get artisans that this client has sent service requests to (who have received quotes)
    @Transactional(readOnly = true)
    public Page<ArtisanResponse> getContactedArtisans(Utilisateur current, Pageable pageable) {
        Long clientId = current.getId();
        
        // Query to find distinct artisans who have sent quotes to this client's requests
        String jpql = """
            SELECT DISTINCT q.artisan FROM Quote q 
            WHERE q.request.client.id = :clientId 
            ORDER BY q.artisan.nom, q.artisan.prenom
            """;
        
        TypedQuery<Artisan> query = entityManager.createQuery(jpql, Artisan.class)
                .setParameter("clientId", clientId);
        
        // Get total count
        String countJpql = """
            SELECT COUNT(DISTINCT q.artisan) FROM Quote q 
            WHERE q.request.client.id = :clientId
            """;
        Long totalCount = entityManager.createQuery(countJpql, Long.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
        
        // Apply pagination
        List<Artisan> artisans = query
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        
        List<ArtisanResponse> artisanResponses = artisans.stream()
                .map(this::toArtisanResponse)
                .toList();
        
        return new PageImpl<>(artisanResponses, pageable, totalCount);
    }

    // ---------- Admin (ROLE_ADMIN) ----------

    @Transactional(readOnly = true)
    public Page<ClientResponse> adminList(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return clientRepository.findAll(pageable).map(this::toResponse);
        }
        String like = q.trim();
        return clientRepository
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelephoneContainingIgnoreCase(
                        like, like, like, like, pageable
                )
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ClientResponse adminGet(Long id) {
        Client c = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        return toResponse(c);
    }

    @Transactional
    public ClientResponse adminUpdate(Long id, ClientUpdateRequest req) {
        Client c = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        c.setNom(req.getNom().trim());
        c.setPrenom(req.getPrenom());
        c.setTelephone(req.getTelephone());

        clientRepository.save(c);
        return toResponse(c);
    }

    @Transactional
    public void adminDelete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new IllegalArgumentException("Client not found");
        }
        clientRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public long getTotalClientsCount() {
        return clientRepository.count();
    }

    // ---------- Mappers ----------

    private ClientResponse toResponse(Client c) {
        return new ClientResponse(
                c.getId(),
                c.getNom(),
                c.getPrenom(),
                c.getEmail(),
                c.getTelephone()
        );
    }

    private ArtisanResponse toArtisanResponse(Artisan a) {
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
