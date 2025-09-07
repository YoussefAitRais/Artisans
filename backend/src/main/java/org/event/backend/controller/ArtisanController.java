package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.artisan.ArtisanResponse;
import org.event.backend.dto.artisan.ArtisanUpdateRequest;
import org.event.backend.dto.inbox.ArtisanInboxItemResponse;
import org.event.backend.dto.review.ReviewResponse;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.artisan.ArtisanService;
import org.event.backend.service.inbox.ArtisanInboxService;
import org.event.backend.service.reviews.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ArtisanController {

    private final ArtisanService artisanService;
    private final ArtisanInboxService inboxService;
    private final ReviewService reviewService;

    public ArtisanController(ArtisanService artisanService,
                             ArtisanInboxService inboxService,
                             ReviewService reviewService) {
        this.artisanService = artisanService;
        this.inboxService = inboxService;
        this.reviewService = reviewService;
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
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ArtisanResponse> me(@AuthenticationPrincipal Utilisateur current) {
        return ResponseEntity.ok(artisanService.getMe(current));
    }

    @PutMapping("/api/artisan/me")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ArtisanResponse> updateMe(@AuthenticationPrincipal Utilisateur current,
                                                    @Valid @RequestBody ArtisanUpdateRequest req) {
        return ResponseEntity.ok(artisanService.updateMe(current, req));
    }

    // ---------- Inbox engagement ----------
    // This version matches when page/size are NOT explicitly present
    @GetMapping(value = "/api/artisan/inbox", params = {"!page","!size"})
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<Page<ArtisanInboxItemResponse>> inbox(
            @AuthenticationPrincipal Utilisateur current,
            Pageable pageable,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String q
    ) {
        Artisan me = asArtisan(current);
        String normalized = status == null ? null : status.trim().toUpperCase();
        String filter = "ALL".equals(normalized) ? null : normalized;
        return ResponseEntity.ok(inboxService.listInbox(me, pageable, filter, q));
    }

    @GetMapping("/api/artisan/reviews")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<Page<ReviewResponse>> myReviewsAsArtisan(
            @AuthenticationPrincipal Utilisateur current, Pageable pageable) {
        Artisan me = asArtisan(current);
        return ResponseEntity.ok(reviewService.getReviewsForArtisan(me.getId(), pageable));
    }

    // ---------- Admin (ROLE_ADMIN) ----------
    @GetMapping("/api/admin/artisans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ArtisanResponse>> adminList(Pageable pageable) {
        return ResponseEntity.ok(artisanService.adminList(pageable));
    }

    @GetMapping("/api/admin/artisans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanResponse> adminGet(@PathVariable Long id) {
        return ResponseEntity.ok(artisanService.adminGet(id));
    }

    @PutMapping("/api/admin/artisans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanResponse> adminUpdate(@PathVariable Long id,
                                                       @Valid @RequestBody ArtisanUpdateRequest req) {
        return ResponseEntity.ok(artisanService.adminUpdate(id, req));
    }

    @DeleteMapping("/api/admin/artisans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        artisanService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }

    // ==== helpers ====
    private Artisan asArtisan(Utilisateur current) {
        if (!(current instanceof Artisan a)) {
            throw new AccessDeniedException("USER_IS_NOT_ARTISAN");
        }
        return a;
    }
}
