package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.review.ReviewCreateRequest;
import org.event.backend.dto.review.ReviewResponse;
import org.event.backend.dto.review.ReviewUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Client;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.reviews.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {

    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) { this.reviewService = reviewService; }

    // -------- Public (artisan profile) --------
    @GetMapping("/api/artisans/{artisanId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getForArtisan(@PathVariable Long artisanId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsForArtisan(artisanId, pageable));
    }

    // -------- Client (owner) --------
    @GetMapping("/api/reviews/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<ReviewResponse>> myReviews(
            @AuthenticationPrincipal Utilisateur current, Pageable pageable) {
        Client client = asClient(current);
        return ResponseEntity.ok(reviewService.myReviews(client, pageable));
    }

    @PostMapping("/api/reviews")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReviewResponse> create(
            @AuthenticationPrincipal Utilisateur current,
            @Valid @RequestBody ReviewCreateRequest req) {
        Client client = asClient(current);
        return ResponseEntity.ok(reviewService.create(client, req));
    }

    @PutMapping("/api/reviews/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReviewResponse> update(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest req) {
        Client client = asClient(current);
        return ResponseEntity.ok(reviewService.update(client, id, req));
    }

    @DeleteMapping("/api/reviews/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Utilisateur current, @PathVariable Long id) {
        Client client = asClient(current);
        reviewService.delete(client, id);
        return ResponseEntity.noContent().build();
    }

    // -------- Admin (moderation) --------
    @GetMapping("/api/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewResponse>> adminList(Pageable pageable) {
        return ResponseEntity.ok(reviewService.adminList(pageable));
    }

    @DeleteMapping("/api/admin/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        reviewService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }

    // -------- Artisan (self) --------
    // RENAMED to avoid collision with ArtisanController mapping
    @GetMapping("/api/artisan/my-reviews")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<Page<ReviewResponse>> myReviewsAsArtisan(
            @AuthenticationPrincipal Utilisateur current, Pageable pageable) {
        Artisan me = asArtisan(current);
        return ResponseEntity.ok(reviewService.getReviewsForArtisan(me.getId(), pageable));
    }

    // ---- helpers ----
    private Client asClient(Utilisateur u) {
        if (u instanceof Client c) return c;
        throw new AccessDeniedException("USER_IS_NOT_CLIENT");
    }

    private Artisan asArtisan(Utilisateur u) {
        if (u instanceof Artisan a) return a;
        throw new AccessDeniedException("USER_IS_NOT_ARTISAN");
    }
}
