package org.event.backend.service;

import org.event.backend.dto.ServiceRequestCreateRequest;
import org.event.backend.dto.ServiceRequestResponse;
import org.event.backend.dto.ServiceRequestUpdateRequest;
import org.event.backend.entity.*;
import org.event.backend.repository.category.CategoryRepository;
import org.event.backend.repository.ServiceRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for client requests (devis).
 */
@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final CategoryRepository categoryRepository;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
                                 CategoryRepository categoryRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.categoryRepository = categoryRepository;
    }

    // -------- Client (owner) --------

    @Transactional
    public ServiceRequestResponse createAsClient(Client client, ServiceRequestCreateRequest req) {
        ServiceRequest sr = new ServiceRequest();
        sr.setClient(client);
        sr.setTitle(req.getTitle().trim());
        sr.setCity(req.getCity());
        sr.setDescription(req.getDescription());
        sr.setDesiredDate(req.getDesiredDate());
        sr.setStatus(RequestStatus.PENDING);

        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            sr.setCategory(cat);
        }

        ServiceRequest saved = serviceRequestRepository.save(sr);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> myRequests(Client client, Pageable pageable) {
        return serviceRequestRepository.findByClient_Id(client.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse getMyRequestById(Client client, Long id) {
        ServiceRequest sr = serviceRequestRepository.findByIdAndClient_Id(id, client.getId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        return toResponse(sr);
    }

    @Transactional
    public ServiceRequestResponse updateMyRequest(Client client, Long id, ServiceRequestUpdateRequest req) {
        ServiceRequest sr = serviceRequestRepository.findByIdAndClient_Id(id, client.getId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (sr.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be updated");
        }

        sr.setTitle(req.getTitle().trim());
        sr.setCity(req.getCity());
        sr.setDescription(req.getDescription());
        sr.setDesiredDate(req.getDesiredDate());

        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            sr.setCategory(cat);
        } else {
            sr.setCategory(null);
        }

        return toResponse(sr); // managed entity
    }

    @Transactional
    public void cancelMyRequest(Client client, Long id) {
        ServiceRequest sr = serviceRequestRepository.findByIdAndClient_Id(id, client.getId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (sr.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be cancelled");
        }
        sr.setStatus(RequestStatus.CANCELLED);
    }

    @Transactional
    public void deleteMyRequest(Client client, Long id) {
        ServiceRequest sr = serviceRequestRepository.findByIdAndClient_Id(id, client.getId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (sr.getStatus() != RequestStatus.PENDING && sr.getStatus() != RequestStatus.CANCELLED) {
            throw new IllegalStateException("Only PENDING or CANCELLED requests can be deleted");
        }
        serviceRequestRepository.delete(sr);
    }

    // -------- Artisan (listing suitable requests) --------

    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> artisanBrowse(Artisan artisan,
                                                      Long categoryId,
                                                      String city,
                                                      Pageable pageable) {
        Specification<ServiceRequest> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            // show only open requests
            preds.add(cb.equal(root.get("status"), RequestStatus.PENDING));

            if (categoryId != null) {
                preds.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (city != null && !city.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase().trim() + "%"));
            }
            // optionally could match artisan.metier to category later
            return cb.and(preds.toArray(new Predicate[0]));
        };

        return serviceRequestRepository.findAll(spec, pageable).map(this::toResponse);
    }

    // -------- Admin --------

    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> adminList(Long categoryId, RequestStatus status, String city, Pageable pageable) {
        Specification<ServiceRequest> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            if (categoryId != null) preds.add(cb.equal(root.get("category").get("id"), categoryId));
            if (city != null && !city.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase().trim() + "%"));
            }
            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(new Predicate[0]));
        };
        return serviceRequestRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse adminGet(Long id) {
        ServiceRequest sr = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        return toResponse(sr);
    }

    @Transactional
    public ServiceRequestResponse adminUpdateStatus(Long id, RequestStatus newStatus) {
        ServiceRequest sr = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        sr.setStatus(newStatus);
        return toResponse(sr);
    }

    @Transactional
    public void adminDelete(Long id) {
        if (!serviceRequestRepository.existsById(id)) {
            throw new IllegalArgumentException("Request not found");
        }
        serviceRequestRepository.deleteById(id);
    }

    // -------- Mapper --------

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
