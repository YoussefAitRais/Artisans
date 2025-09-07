// src/main/java/org/event/backend/service/client/ClientRequestService.java
package org.event.backend.service.client;

import org.event.backend.dto.ServiceRequestCreateRequest;
import org.event.backend.dto.ServiceRequestResponse;
import org.event.backend.entity.Category;
import org.event.backend.entity.Client;
import org.event.backend.entity.RequestStatus;
import org.event.backend.entity.ServiceRequest;
import org.event.backend.repository.CategoryRepository;
import org.event.backend.repository.ServiceRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientRequestService {

    private final ServiceRequestRepository requestRepo;
    private final CategoryRepository categoryRepo;

    public ClientRequestService(ServiceRequestRepository r, CategoryRepository c) {
        this.requestRepo = r;
        this.categoryRepo = c;
    }

    @Transactional
    public ServiceRequestResponse create(Client client, ServiceRequestCreateRequest req) {
        ServiceRequest sr = new ServiceRequest();
        sr.setClient(client);
        sr.setTitle(req.getTitle().trim());
        sr.setCity(req.getCity());
        sr.setDescription(req.getDescription());
        sr.setDesiredDate(req.getDesiredDate());
        sr.setStatus(RequestStatus.PENDING);

        if (req.getCategoryId() != null) {
            Category cat = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            sr.setCategory(cat);
        }

        ServiceRequest saved = requestRepo.save(sr);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> myRequests(Client client, Pageable pageable) {
        return requestRepo.findByClient_Id(client.getId(), pageable).map(this::toResponse);
    }

    // ---------- mapper ----------
    private ServiceRequestResponse toResponse(ServiceRequest sr) {
        Long categoryId = (sr.getCategory() != null) ? sr.getCategory().getId() : null;
        String clientEmail = (sr.getClient() != null) ? sr.getClient().getEmail() : null;

        return new ServiceRequestResponse(
                sr.getId(),
                categoryId,
                sr.getTitle(),
                sr.getCity(),
                sr.getDescription(),
                sr.getDesiredDate(),
                sr.getStatus(),
                sr.getCreatedAt(),
                clientEmail
        );
    }
}
