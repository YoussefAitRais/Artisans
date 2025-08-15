package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.engagement.EngagementConfirmRequest;
import org.event.backend.dto.engagement.EngagementResponse;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Client;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.engagement.EngagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST endpoints for engagements (bookings). */
@RestController
@RequestMapping("/api/engagements")
public class EngagementController {

    private final EngagementService engagementService;

    public EngagementController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @GetMapping("/my")
    public ResponseEntity<Page<EngagementResponse>> myEngagements(@AuthenticationPrincipal Utilisateur current,
                                                                  Pageable pageable) {
        return ResponseEntity.ok(engagementService.myEngagements(current, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EngagementResponse> getOne(@AuthenticationPrincipal Utilisateur current,
                                                     @PathVariable Long id) {
        return ResponseEntity.ok(engagementService.getOne(current, id));
    }

    // ---- Transitions ----

    @PostMapping("/{id}/confirm") // ARTISAN
    public ResponseEntity<EngagementResponse> confirm(@AuthenticationPrincipal Utilisateur current,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody EngagementConfirmRequest req) {
        return ResponseEntity.ok(engagementService.confirmAsArtisan((Artisan) current, id, req));
    }

    @PostMapping("/{id}/start") // both
    public ResponseEntity<EngagementResponse> start(@AuthenticationPrincipal Utilisateur current,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(engagementService.start(current, id));
    }

    @PostMapping("/{id}/complete") // CLIENT
    public ResponseEntity<EngagementResponse> complete(@AuthenticationPrincipal Utilisateur current,
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(engagementService.completeAsClient((Client) current, id));
    }

    @PostMapping("/{id}/cancel") // both (before start)
    public ResponseEntity<EngagementResponse> cancel(@AuthenticationPrincipal Utilisateur current,
                                                     @PathVariable Long id) {
        return ResponseEntity.ok(engagementService.cancel(current, id));
    }
}
