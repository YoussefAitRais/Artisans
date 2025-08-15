package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.quote.QuoteCreateRequest;
import org.event.backend.dto.quote.QuoteResponse;
import org.event.backend.dto.quote.QuoteUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Client;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.quote.QuoteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for Quotes (devis).
 *
 * Security expectations:
 * - Artisan-only: create/update/delete own quotes
 * - Client-only: list quotes for own request + accept a quote
 * - Admin: moderate/view lists
 */
@RestController
public class QuoteController {

    private final QuoteService quoteService;
    public QuoteController(QuoteService quoteService) { this.quoteService = quoteService; }

    // ---------- Artisan ----------
    @PostMapping("/api/requests/{requestId}/quotes")
    public ResponseEntity<QuoteResponse> createAsArtisan(@AuthenticationPrincipal Utilisateur current,
                                                         @PathVariable Long requestId,
                                                         @Valid @RequestBody QuoteCreateRequest req) {
        Artisan artisan = (Artisan) current;
        return ResponseEntity.ok(quoteService.createAsArtisan(artisan, requestId, req));
    }

    @PutMapping("/api/quotes/{id}")
    public ResponseEntity<QuoteResponse> updateAsArtisan(@AuthenticationPrincipal Utilisateur current,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody QuoteUpdateRequest req) {
        Artisan artisan = (Artisan) current;
        return ResponseEntity.ok(quoteService.updateAsArtisan(artisan, id, req));
    }

    @DeleteMapping("/api/quotes/{id}")
    public ResponseEntity<Void> deleteAsArtisan(@AuthenticationPrincipal Utilisateur current,
                                                @PathVariable Long id) {
        Artisan artisan = (Artisan) current;
        quoteService.deleteAsArtisan(artisan, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/artisan/quotes")
    public ResponseEntity<Page<QuoteResponse>> myQuotesAsArtisan(@AuthenticationPrincipal Utilisateur current,
                                                                 Pageable pageable) {
        Artisan artisan = (Artisan) current;
        return ResponseEntity.ok(quoteService.myQuotesAsArtisan(artisan, pageable));
    }

    // ---------- Client ----------
    @GetMapping("/api/requests/{requestId}/quotes")
    public ResponseEntity<Page<QuoteResponse>> listQuotesForMyRequest(@AuthenticationPrincipal Utilisateur current,
                                                                      @PathVariable Long requestId,
                                                                      Pageable pageable) {
        Client client = (Client) current;
        return ResponseEntity.ok(quoteService.listQuotesForMyRequest(client, requestId, pageable));
    }

    @PostMapping("/api/quotes/{id}/accept")
    public ResponseEntity<QuoteResponse> acceptQuote(@AuthenticationPrincipal Utilisateur current,
                                                     @PathVariable Long id) {
        Client client = (Client) current;
        return ResponseEntity.ok(quoteService.acceptQuote(client, id));
    }

    // ---------- Admin ----------
    @GetMapping("/api/admin/quotes/by-request/{requestId}")
    public ResponseEntity<Page<QuoteResponse>> adminListByRequest(@PathVariable Long requestId, Pageable pageable) {
        return ResponseEntity.ok(quoteService.adminListByRequest(requestId, pageable));
    }

    @GetMapping("/api/admin/quotes/by-artisan/{artisanId}")
    public ResponseEntity<Page<QuoteResponse>> adminListByArtisan(@PathVariable Long artisanId, Pageable pageable) {
        return ResponseEntity.ok(quoteService.adminListByArtisan(artisanId, pageable));
    }

    @DeleteMapping("/api/admin/quotes/{id}")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        quoteService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}
