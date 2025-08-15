package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.media.MediaResponse;
import org.event.backend.dto.media.MediaUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.media.MediaService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Portfolio endpoints:
 * - Public listing: GET /api/artisans/{id}/portfolio
 * - Owner (artisan): upload/list/update/delete/set-cover under /api/artisan/...
 * - File streaming: GET /api/media/{id}/file
 */
@RestController
public class MediaController {

    private final MediaService mediaService;
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // ---------- Public ----------
    @GetMapping("/api/artisans/{artisanId}/portfolio")
    public ResponseEntity<List<MediaResponse>> publicPortfolio(@PathVariable Long artisanId) {
        return ResponseEntity.ok(mediaService.publicPortfolio(artisanId));
    }

    // ---------- Owner (ROLE_ARTISAN) ----------
    @GetMapping("/api/artisan/me/portfolio")
    public ResponseEntity<List<MediaResponse>> myPortfolio(@AuthenticationPrincipal Utilisateur current) {
        Artisan artisan = (Artisan) current;
        return ResponseEntity.ok(mediaService.myPortfolio(artisan));
    }

    @PostMapping(path = "/api/artisan/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> upload(@AuthenticationPrincipal Utilisateur current,
                                                @RequestPart("file") MultipartFile file,
                                                @RequestParam(value = "title", required = false) String title,
                                                @RequestParam(value = "description", required = false) String description,
                                                @RequestParam(value = "tags", required = false) String tags,
                                                @RequestParam(value = "isPublic", required = false) Boolean isPublic) {
        Artisan artisan = (Artisan) current;
        return ResponseEntity.ok(mediaService.upload(artisan, file, title, description, tags, isPublic));
    }

    @PutMapping("/api/artisan/media/{id}")
    public ResponseEntity<MediaResponse> update(@AuthenticationPrincipal Utilisateur current,
                                                @PathVariable Long id,
                                                @Valid @RequestBody MediaUpdateRequest req) {
        Artisan artisan = (Artisan) current;
        return ResponseEntity.ok(mediaService.updateMetadata(artisan, id, req));
    }

    @PostMapping("/api/artisan/media/{id}/cover")
    public ResponseEntity<Void> setCover(@AuthenticationPrincipal Utilisateur current,
                                         @PathVariable Long id) {
        Artisan artisan = (Artisan) current;
        mediaService.setCover(artisan, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/artisan/media/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Utilisateur current,
                                       @PathVariable Long id) {
        Artisan artisan = (Artisan) current;
        mediaService.delete(artisan, id);
        return ResponseEntity.noContent().build();
    }

    // ---------- File streaming (public) ----------
    @GetMapping("/api/media/{id}/file")
    public ResponseEntity<Resource> stream(@PathVariable Long id) {
        Resource file = mediaService.loadAsResource(id);
        // Let the browser decide how to render it
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000") // 1y cache (dev-friendly)
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // or try detect via DB field
                .body(file);
    }
}
