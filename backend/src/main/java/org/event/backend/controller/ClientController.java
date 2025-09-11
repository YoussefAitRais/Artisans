package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.artisan.ArtisanResponse;
import org.event.backend.dto.client.ClientResponse;
import org.event.backend.dto.client.ClientUpdateRequest;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.client.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


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

    // Get artisans that this client has sent service requests to
    @GetMapping("/api/client/me/contacted-artisans")
    public ResponseEntity<Page<ArtisanResponse>> getContactedArtisans(
            @AuthenticationPrincipal Utilisateur current,
            Pageable pageable
    ) {
        return ResponseEntity.ok(clientService.getContactedArtisans(current, pageable));
    }


}
