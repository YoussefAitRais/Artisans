package org.event.backend.service.client;

import org.event.backend.dto.client.ClientResponse;
import org.event.backend.dto.client.ClientUpdateRequest;
import org.event.backend.entity.Client;
import org.event.backend.entity.Utilisateur;
import org.event.backend.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for client profiles: self-get/update and admin ops.
 */
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
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

    // ---------- Mapper ----------

    private ClientResponse toResponse(Client c) {
        return new ClientResponse(
                c.getId(),
                c.getNom(),
                c.getPrenom(),
                c.getEmail(),
                c.getTelephone()
        );
    }
}
