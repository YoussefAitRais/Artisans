package org.event.backend.controller;

import org.event.backend.dto.inbox.ArtisanInboxItemResponse;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.inbox.ArtisanInboxService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artisan")
public class ArtisanInboxController {

    private final ArtisanInboxService inboxService;

    public ArtisanInboxController(ArtisanInboxService inboxService) {
        this.inboxService = inboxService;
    }

    // This version matches ONLY when both page and size are present
    @GetMapping(value = "/inbox", params = {"page","size"})
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<Page<ArtisanInboxItemResponse>> inboxLegacy(
            @AuthenticationPrincipal Utilisateur current,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String q
    ) {
        if (!(current instanceof Artisan artisan)) {
            return ResponseEntity.status(403).build();
        }
        String normalized = status == null ? null : status.trim().toUpperCase();
        String filter = "ALL".equals(normalized) ? null : normalized;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(inboxService.listInbox(artisan, pageable, filter, q));
    }
}
