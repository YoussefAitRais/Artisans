package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.ServiceRequestCreateRequest;
import org.event.backend.dto.ServiceRequestResponse;
import org.event.backend.dto.ServiceRequestUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Client;
import org.event.backend.entity.RequestStatus;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.ServiceRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for service requests.
 *
 * Security expectations (align with your SecurityConfig):
 * - /api/requests/**           -> ROLE_CLIENT (owner)
 * - /api/artisan/requests/**   -> ROLE_ARTISAN (browse open requests)
 * - /api/admin/requests/**     -> ROLE_ADMIN
 */
@RestController
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    // ---------- Client (owner) ----------

    @PostMapping("/api/requests")
    public ResponseEntity<ServiceRequestResponse> createAsClient(@AuthenticationPrincipal Utilisateur current,
                                                                 @Valid @RequestBody ServiceRequestCreateRequest req) {
        Client client = (Client) current; // Utilisateur is stored; casting assumes correct role mapping
        ServiceRequestResponse created = serviceRequestService.createAsClient(client, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/requests")
    public ResponseEntity<Page<ServiceRequestResponse>> myRequests(@AuthenticationPrincipal Utilisateur current,
                                                                   Pageable pageable) {
        Client client = (Client) current;
        return ResponseEntity.ok(serviceRequestService.myRequests(client, pageable));
    }

    @GetMapping("/api/requests/{id}")
    public ResponseEntity<ServiceRequestResponse> getMyRequest(@AuthenticationPrincipal Utilisateur current,
                                                               @PathVariable Long id) {
        Client client = (Client) current;
        return ResponseEntity.ok(serviceRequestService.getMyRequestById(client, id));
    }

    @PutMapping("/api/requests/{id}")
    public ResponseEntity<ServiceRequestResponse> updateMyRequest(@AuthenticationPrincipal Utilisateur current,
                                                                  @PathVariable Long id,
                                                                  @Valid @RequestBody ServiceRequestUpdateRequest req) {
        Client client = (Client) current;
        return ResponseEntity.ok(serviceRequestService.updateMyRequest(client, id, req));
    }

    @PostMapping("/api/requests/{id}/cancel")
    public ResponseEntity<Void> cancelMyRequest(@AuthenticationPrincipal Utilisateur current,
                                                @PathVariable Long id) {
        Client client = (Client) current;
        serviceRequestService.cancelMyRequest(client, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/requests/{id}")
    public ResponseEntity<Void> deleteMyRequest(@AuthenticationPrincipal Utilisateur current,
                                                @PathVariable Long id) {
        Client client = (Client) current;
        serviceRequestService.deleteMyRequest(client, id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Artisan (browse open requests) ----------

    @GetMapping("/api/artisan/requests")
    public ResponseEntity<Page<ServiceRequestResponse>> artisanBrowse(@AuthenticationPrincipal Utilisateur current,
                                                                      @RequestParam(required = false) Long categoryId,
                                                                      @RequestParam(required = false) String city,
                                                                      Pageable pageable) {
        Artisan artisan = (Artisan) current;
        return ResponseEntity.ok(serviceRequestService.artisanBrowse(artisan, categoryId, city, pageable));
    }

    // ---------- Admin ----------

    @GetMapping("/api/admin/requests")
    public ResponseEntity<Page<ServiceRequestResponse>> adminList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) String city,
            Pageable pageable
    ) {
        return ResponseEntity.ok(serviceRequestService.adminList(categoryId, status, city, pageable));
    }

    @GetMapping("/api/admin/requests/{id}")
    public ResponseEntity<ServiceRequestResponse> adminGet(@PathVariable Long id) {
        return ResponseEntity.ok(serviceRequestService.adminGet(id));
    }

    @PatchMapping("/api/admin/requests/{id}/status")
    public ResponseEntity<ServiceRequestResponse> adminUpdateStatus(@PathVariable Long id,
                                                                    @RequestParam RequestStatus status) {
        return ResponseEntity.ok(serviceRequestService.adminUpdateStatus(id, status));
    }

    @DeleteMapping("/api/admin/requests/{id}")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        serviceRequestService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}
