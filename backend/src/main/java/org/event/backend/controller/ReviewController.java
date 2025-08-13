package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.review.ReviewCreateRequest;
import org.event.backend.dto.review.ReviewResponse;
import org.event.backend.dto.review.ReviewUpdateRequest;
import org.event.backend.entity.Client;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.reviews.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for reviews:
 * - Public: GET /api/artisans/{artisanId}/reviews
 * - Client (ROLE_CLIENT): CRUD on own reviews under /api/reviews
 * - Admin (ROLE_ADMIN): moderate under /api/admin/reviews
 */
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
    public ResponseEntity<Page<ReviewResponse>> myReviews(@AuthenticationPrincipal Utilisateur current, Pageable pageable) {
        Client client = (Client) current;
        return ResponseEntity.ok(reviewService.myReviews(client, pageable));
    }

    @PostMapping("/api/reviews")
    public ResponseEntity<ReviewResponse> create(@AuthenticationPrincipal Utilisateur current,
                                                 @Valid @RequestBody ReviewCreateRequest req) {
        Client client = (Client) current;
        return ResponseEntity.ok(reviewService.create(client, req));
    }

    @PutMapping("/api/reviews/{id}")
    public ResponseEntity<ReviewResponse> update(@AuthenticationPrincipal Utilisateur current,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody ReviewUpdateRequest req) {
        Client client = (Client) current;
        return ResponseEntity.ok(reviewService.update(client, id, req));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Utilisateur current, @PathVariable Long id) {
        Client client = (Client) current;
        reviewService.delete(client, id);
        return ResponseEntity.noContent().build();
    }

    // -------- Admin (moderation) --------
    @GetMapping("/api/admin/reviews")
    public ResponseEntity<Page<ReviewResponse>> adminList(Pageable pageable) {
        return ResponseEntity.ok(reviewService.adminList(pageable));
    }

    @DeleteMapping("/api/admin/reviews/{id}")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        reviewService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}
