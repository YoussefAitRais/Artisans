package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.client.ClientResponse;
import org.event.backend.dto.client.ClientUpdateRequest;
import org.event.backend.service.client.ClientService;
import org.event.backend.service.ServiceRequestService;
import org.event.backend.repository.ArtisanRepository;
import org.event.backend.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AdminController {

    private final ClientService clientService;
    private final ServiceRequestService serviceRequestService;
    private final ArtisanRepository artisanRepository;
    private final CategoryRepository categoryRepository;

    public AdminController(ClientService clientService,
                          ServiceRequestService serviceRequestService,
                          ArtisanRepository artisanRepository,
                          CategoryRepository categoryRepository) {
        this.clientService = clientService;
        this.serviceRequestService = serviceRequestService;
        this.artisanRepository = artisanRepository;
        this.categoryRepository = categoryRepository;
    }

    // ---------- Admin (ROLE_ADMIN) ----------
    @GetMapping("/api/admin/clients")
    public ResponseEntity<Page<ClientResponse>> adminList(
            @RequestParam(required = false, name = "q") String query,
            Pageable pageable
    ) {
        return ResponseEntity.ok(clientService.adminList(query, pageable));
    }

    @GetMapping("/api/admin/clients/{id}")
    public ResponseEntity<ClientResponse> adminGet(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.adminGet(id));
    }

    @PutMapping("/api/admin/clients/{id}")
    public ResponseEntity<ClientResponse> adminUpdate(@PathVariable Long id,
                                                      @Valid @RequestBody ClientUpdateRequest req) {
        return ResponseEntity.ok(clientService.adminUpdate(id, req));
    }

    @DeleteMapping("/api/admin/clients/{id}")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        clientService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Admin Stats ----------
    @GetMapping("/api/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get counts from repositories
        long clientsCount = clientService.getTotalClientsCount();
        long artisansCount = artisanRepository.count();
        long demandesCount = serviceRequestService.getTotalRequestsCount();
        long avisCount = 0; // TODO: Implement reviews count when ReviewService is available
        
        stats.put("clients", clientsCount);
        stats.put("artisans", artisansCount);
        stats.put("demandes", demandesCount);
        stats.put("avis", avisCount);
        
        return ResponseEntity.ok(stats);
    }
}
