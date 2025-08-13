package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.client.ClientResponse;
import org.event.backend.dto.client.ClientUpdateRequest;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.client.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Client CRUD:
 * - Self (ROLE_CLIENT): GET/PUT /api/client/me
 * - Admin (ROLE_ADMIN): /api/admin/clients[...]
 */
@RestController
public class ClientController {

    private final ClientService clientService;
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // ---------- Self (ROLE_CLIENT) ----------
    @GetMapping("/api/client/me")
    public ResponseEntity<ClientResponse> me(@AuthenticationPrincipal Utilisateur current) {
        return ResponseEntity.ok(clientService.getMe(current));
    }

    @PutMapping("/api/client/me")
    public ResponseEntity<ClientResponse> updateMe(@AuthenticationPrincipal Utilisateur current,
                                                   @Valid @RequestBody ClientUpdateRequest req) {
        return ResponseEntity.ok(clientService.updateMe(current, req));
    }

    // ---------- Admin (ROLE_ADMIN) ----------
    @GetMapping("/api/admin/clients")
    public ResponseEntity<Page<ClientResponse>> adminList(
            @RequestParam(required = false, name = "q") String query,
            Pageable pageable
    ) {
        return ResponseEntity.ok(clientService.adminList(query, pageable));
    }

    @GetMapping("/api/admin/clients/{id}")
    public ResponseEntity<ClientResponse> adminGet(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.adminGet(id));
    }

    @PutMapping("/api/admin/clients/{id}")
    public ResponseEntity<ClientResponse> adminUpdate(@PathVariable Long id,
                                                      @Valid @RequestBody ClientUpdateRequest req) {
        return ResponseEntity.ok(clientService.adminUpdate(id, req));
    }

    @DeleteMapping("/api/admin/clients/{id}")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        clientService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}
