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

@RestController
@RequestMapping("/api/engagements")
public class EngagementController {

    private final EngagementService engagementService;

    public EngagementController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @GetMapping(path = "/mine")
    public ResponseEntity<Page<EngagementResponse>> mine(
            @AuthenticationPrincipal Utilisateur current,
            Pageable pageable) {
        return ResponseEntity.ok(engagementService.myEngagements(current, pageable));
    }

    @GetMapping(path = "/{id:\\d+}")
    public ResponseEntity<EngagementResponse> getOne(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(engagementService.getOne(current, id));
    }

    @PostMapping(path = "/{id:\\d+}/confirm")
    public ResponseEntity<EngagementResponse> confirm(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable("id") Long id,
            @Valid @RequestBody EngagementConfirmRequest req) {
        return ResponseEntity.ok(engagementService.confirmAsArtisan((Artisan) current, id, req));
    }

    @PostMapping(path = "/{id:\\d+}/start")
    public ResponseEntity<EngagementResponse> start(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(engagementService.start(current, id));
    }

    @PostMapping(path = "/{id:\\d+}/complete")
    public ResponseEntity<EngagementResponse> complete(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(engagementService.completeAsClient((Client) current, id));
    }

    @PostMapping(path = "/{id:\\d+}/cancel")
    public ResponseEntity<EngagementResponse> cancel(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(engagementService.cancel(current, id));
    }
}
