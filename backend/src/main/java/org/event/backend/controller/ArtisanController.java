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

/**
 * REST Controller for managing artisan-related operations.
 * 
 * Provides endpoints for:
 * - Public artisan search and profile viewing
 * - Artisan self-service operations (profile management)
 * - Administrative artisan management
 * - Artisan inbox and review management
 * 
 * @author Artisan Platform Team
 * @version 1.0
 */

@RestController
public class ArtisanController {

    private final ArtisanService artisanService;
    private final ArtisanInboxService inboxService;
    private final ReviewService reviewService;

    /**
     * Constructor for dependency injection.
     * 
     * @param artisanService service for artisan operations
     * @param inboxService service for artisan inbox management
     * @param reviewService service for review management
     */
    public ArtisanController(ArtisanService artisanService,
                             ArtisanInboxService inboxService,
                             ReviewService reviewService) {
        this.artisanService = artisanService;
        this.inboxService = inboxService;
        this.reviewService = reviewService;
    }

    // ============ PUBLIC ENDPOINTS ============
    
    /**
     * Searches for artisans with optional filtering.
     * 
     * @param metier profession/trade filter (optional)
     * @param localisation location filter (optional)
     * @param query general search query (optional)
     * @param pageable pagination parameters
     * @return paginated list of matching artisans
     */
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

    /**
     * Retrieves a specific artisan's public profile.
     * 
     * @param id the artisan's unique identifier
     * @return the artisan's profile information
     */
    @GetMapping("/api/artisans/{id}")
    public ResponseEntity<ArtisanResponse> getArtisanById(@PathVariable Long id) {
        ArtisanResponse artisan = artisanService.getArtisanById(id);
        return ResponseEntity.ok(artisan);
    }

    // ============ ARTISAN SELF-SERVICE ENDPOINTS ============
    
    /**
     * Retrieves the current artisan's own profile.
     * 
     * @param currentUser the authenticated artisan user
     * @return the artisan's profile information
     */
    @GetMapping("/api/artisan/me")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ArtisanResponse> getCurrentArtisanProfile(@AuthenticationPrincipal Utilisateur currentUser) {
        ArtisanResponse profile = artisanService.getCurrentArtisanProfile(currentUser);
        return ResponseEntity.ok(profile);
    }

    /**
     * Updates the current artisan's profile information.
     * 
     * @param currentUser the authenticated artisan user
     * @param updateRequest the profile update data
     * @return the updated artisan profile
     */
    @PutMapping("/api/artisan/me")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ArtisanResponse> updateCurrentArtisanProfile(
            @AuthenticationPrincipal Utilisateur currentUser,
            @Valid @RequestBody ArtisanUpdateRequest updateRequest) {
        ArtisanResponse updatedProfile = artisanService.updateCurrentArtisanProfile(currentUser, updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    // ============ ARTISAN INBOX AND REVIEW ENDPOINTS ============
    
    /**
     * Retrieves the artisan's inbox with optional filtering.
     * Note: This endpoint matches when page/size parameters are NOT explicitly present.
     * 
     * @param currentUser the authenticated artisan user
     * @param pageable pagination parameters
     * @param status engagement status filter (defaults to ALL)
     * @param query search query for filtering inbox items
     * @return paginated list of inbox items
     */
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

    /**
     * Retrieves reviews for the current artisan.
     * 
     * @param currentUser the authenticated artisan user
     * @param pageable pagination parameters
     * @return paginated list of reviews for the artisan
     */
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
    
    /**
     * Retrieves all artisans for administrative purposes.
     * 
     * @param pageable pagination parameters
     * @return paginated list of all artisans
     */
    @GetMapping("/api/admin/artisans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ArtisanResponse>> getAllArtisansForAdmin(Pageable pageable) {
        Page<ArtisanResponse> artisans = artisanService.getAllArtisansForAdmin(pageable);
        return ResponseEntity.ok(artisans);
    }

    /**
     * Retrieves a specific artisan for administrative purposes.
     * 
     * @param id the artisan's unique identifier
     * @return the artisan's profile information
     */
    @GetMapping("/api/admin/artisans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanResponse> getArtisanForAdmin(@PathVariable Long id) {
        ArtisanResponse artisan = artisanService.getArtisanForAdmin(id);
        return ResponseEntity.ok(artisan);
    }

    /**
     * Updates an artisan's profile as an administrator.
     * 
     * @param id the artisan's unique identifier
     * @param updateRequest the profile update data
     * @return the updated artisan profile
     */
    @PutMapping("/api/admin/artisans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanResponse> updateArtisanAsAdmin(
            @PathVariable Long id,
            @Valid @RequestBody ArtisanUpdateRequest updateRequest) {
        ArtisanResponse updatedArtisan = artisanService.updateArtisanAsAdmin(id, updateRequest);
        return ResponseEntity.ok(updatedArtisan);
    }

    /**
     * Deletes an artisan profile as an administrator.
     * 
     * @param id the artisan's unique identifier
     * @return empty response with 204 No Content status
     */
    @DeleteMapping("/api/admin/artisans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteArtisanAsAdmin(@PathVariable Long id) {
        artisanService.deleteArtisanAsAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ============ PRIVATE HELPER METHODS ============
    
    /**
     * Validates that the current user is an artisan and returns the artisan entity.
     * 
     * @param currentUser the authenticated user
     * @return the validated artisan entity
     * @throws AccessDeniedException if the user is not an artisan
     */
    private Artisan validateAndGetArtisan(Utilisateur currentUser) {
        if (!(currentUser instanceof Artisan artisan)) {
            throw new AccessDeniedException("Access denied: User is not an artisan");
        }
        return artisan;
    }
    
    /**
     * Normalizes status filter by converting to uppercase and handling "ALL" case.
     * 
     * @param status the raw status filter
     * @return normalized status filter (null for "ALL", uppercase otherwise)
     */
    private String normalizeStatusFilter(String status) {
        if (status == null) {
            return null;
        }
        
        String normalized = status.trim().toUpperCase();
        return "ALL".equals(normalized) ? null : normalized;
    }
}
