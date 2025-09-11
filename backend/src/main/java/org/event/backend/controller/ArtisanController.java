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

    // ============ PUBLIC ENDPOINTS ============
    

    @GetMapping("/api/artisans")
    public ResponseEntity<Page<ArtisanResponse>> searchArtisans(
            @RequestParam(required = false) String metier,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false, name = "q") String query,
            Pageable pageable
    ) {
        Page<ArtisanResponse> artisans = artisanService.searchArtisans(metier, localisation, query, pageable);
        return ResponseEntity.ok(artisans);
    }


    @GetMapping("/api/artisans/{id}")
    public ResponseEntity<ArtisanResponse> getArtisanById(@PathVariable Long id) {
        ArtisanResponse artisan = artisanService.getArtisanById(id);
        return ResponseEntity.ok(artisan);
    }

    // ============ ARTISAN SELF-SERVICE ENDPOINTS ============
    

    @GetMapping("/api/artisan/me")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ArtisanResponse> getCurrentArtisanProfile(@AuthenticationPrincipal Utilisateur currentUser) {
        ArtisanResponse profile = artisanService.getCurrentArtisanProfile(currentUser);
        return ResponseEntity.ok(profile);
    }


    @PutMapping("/api/artisan/me")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ArtisanResponse> updateCurrentArtisanProfile(
            @AuthenticationPrincipal Utilisateur currentUser,
            @Valid @RequestBody ArtisanUpdateRequest updateRequest) {
        ArtisanResponse updatedProfile = artisanService.updateCurrentArtisanProfile(currentUser, updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    // ============ ARTISAN INBOX AND REVIEW ENDPOINTS ============
    

    @GetMapping(value = "/api/artisan/inbox", params = {"!page", "!size"})
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<Page<ArtisanInboxItemResponse>> getArtisanInbox(
            @AuthenticationPrincipal Utilisateur currentUser,
            Pageable pageable,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String query
    ) {
        Artisan artisan = validateAndGetArtisan(currentUser);
        String statusFilter = normalizeStatusFilter(status);
        
        Page<ArtisanInboxItemResponse> inboxItems = inboxService.getArtisanInbox(artisan, pageable, statusFilter, query);
        return ResponseEntity.ok(inboxItems);
    }


    @GetMapping("/api/artisan/reviews")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<Page<ReviewResponse>> getArtisanReviews(
            @AuthenticationPrincipal Utilisateur currentUser, 
            Pageable pageable) {
        Artisan artisan = validateAndGetArtisan(currentUser);
        Page<ReviewResponse> reviews = reviewService.getReviewsForArtisan(artisan.getId(), pageable);
        return ResponseEntity.ok(reviews);
    }

    // ============ ADMINISTRATIVE ENDPOINTS ============
    

    @GetMapping("/api/admin/artisans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ArtisanResponse>> getAllArtisansForAdmin(Pageable pageable) {
        Page<ArtisanResponse> artisans = artisanService.getAllArtisansForAdmin(pageable);
        return ResponseEntity.ok(artisans);
    }


    @GetMapping("/api/admin/artisans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanResponse> getArtisanForAdmin(@PathVariable Long id) {
        ArtisanResponse artisan = artisanService.getArtisanForAdmin(id);
        return ResponseEntity.ok(artisan);
    }


    @PutMapping("/api/admin/artisans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanResponse> updateArtisanAsAdmin(
            @PathVariable Long id,
            @Valid @RequestBody ArtisanUpdateRequest updateRequest) {
        ArtisanResponse updatedArtisan = artisanService.updateArtisanAsAdmin(id, updateRequest);
        return ResponseEntity.ok(updatedArtisan);
    }


    @DeleteMapping("/api/admin/artisans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteArtisanAsAdmin(@PathVariable Long id) {
        artisanService.deleteArtisanAsAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ============ PRIVATE HELPER METHODS ============
    

    private Artisan validateAndGetArtisan(Utilisateur currentUser) {
        if (!(currentUser instanceof Artisan artisan)) {
            throw new AccessDeniedException("Access denied: User is not an artisan");
        }
        return artisan;
    }
    

    private String normalizeStatusFilter(String status) {
        if (status == null) {
            return null;
        }
        
        String normalized = status.trim().toUpperCase();
        return "ALL".equals(normalized) ? null : normalized;
    }
}
