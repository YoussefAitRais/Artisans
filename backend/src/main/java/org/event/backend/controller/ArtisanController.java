package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.artisan.ArtisanResponse;
import org.event.backend.dto.artisan.ArtisanUpdateRequest;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.artisan.ArtisanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Artisan CRUD:
 * - Public: GET /api/artisans, GET /api/artisans/{id}
 * - Self (ROLE_ARTISAN): GET/PUT /api/artisan/me
 * - Admin (ROLE_ADMIN): /api/admin/artisans[...]
 */
@RestController
public class ArtisanController {

    private final ArtisanService artisanService;

    public ArtisanController(ArtisanService artisanService) {
        this.artisanService = artisanService;
    }

    // ---------- Public ----------
    @GetMapping("/api/artisans")
    public ResponseEntity<Page<ArtisanResponse>> search(
            @RequestParam(required = false) String metier,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false, name = "q") String query,
            Pageable pageable
    ) {
        return ResponseEntity.ok(artisanService.search(metier, localisation, query, pageable));
    }

    @GetMapping("/api/artisans/{id}")
    public ResponseEntity<ArtisanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(artisanService.getById(id));
    }

    // ---------- Self (ROLE_ARTISAN) ----------
    @GetMapping("/api/artisan/me")
    public ResponseEntity<ArtisanResponse> me(@AuthenticationPrincipal Utilisateur current) {
        return ResponseEntity.ok(artisanService.getMe(current));
    }

    @PutMapping("/api/artisan/me")
    public ResponseEntity<ArtisanResponse> updateMe(@AuthenticationPrincipal Utilisateur current,
                                                    @Valid @RequestBody ArtisanUpdateRequest req) {
        return ResponseEntity.ok(artisanService.updateMe(current, req));
    }

    // ---------- Admin (ROLE_ADMIN) ----------
    @GetMapping("/api/admin/artisans")
    public ResponseEntity<Page<ArtisanResponse>> adminList(Pageable pageable) {
        return ResponseEntity.ok(artisanService.adminList(pageable));
    }

    @GetMapping("/api/admin/artisans/{id}")
    public ResponseEntity<ArtisanResponse> adminGet(@PathVariable Long id) {
        return ResponseEntity.ok(artisanService.adminGet(id));
    }

    @PutMapping("/api/admin/artisans/{id}")
    public ResponseEntity<ArtisanResponse> adminUpdate(@PathVariable Long id,
                                                       @Valid @RequestBody ArtisanUpdateRequest req) {
        return ResponseEntity.ok(artisanService.adminUpdate(id, req));
    }

    @DeleteMapping("/api/admin/artisans/{id}")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        artisanService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}
