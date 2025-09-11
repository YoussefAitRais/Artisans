package org.event.backend.service.inbox;

import org.event.backend.dto.inbox.ArtisanInboxItemResponse;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.ServiceRequest;
import org.event.backend.repository.QuoteRepository;
import org.event.backend.repository.ServiceRequestRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArtisanInboxService {

    // ============ DEPENDENCIES ============
    
    private final ServiceRequestRepository serviceRequestRepository;
    private final QuoteRepository quoteRepository;


    public ArtisanInboxService(ServiceRequestRepository serviceRequestRepository, 
                               QuoteRepository quoteRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.quoteRepository = quoteRepository;
    }
    // ============ PUBLIC METHODS ============
    

    public Page<ArtisanInboxItemResponse> getArtisanInbox(Artisan artisan,
                                                          Pageable pageable,
                                                          String statusFilter, 
                                                          String searchQuery) {
        // Get relevant service requests for this artisan
        Page<ServiceRequest> serviceRequests = getAvailableServiceRequests(artisan, pageable);
        
        // Transform to inbox items with response status
        List<ArtisanInboxItemResponse> inboxItems = transformToInboxItems(serviceRequests.getContent(), artisan);
        
        // Apply filters
        List<ArtisanInboxItemResponse> filteredItems = applyFilters(inboxItems, statusFilter, searchQuery);
        
        return new PageImpl<>(filteredItems, pageable, serviceRequests.getTotalElements());
    }
    // ============ PRIVATE HELPER METHODS ============
    

    private Page<ServiceRequest> getAvailableServiceRequests(Artisan artisan, Pageable pageable) {
        Long categoryId = getCategoryId(artisan);
        return serviceRequestRepository.findByCategoryWithoutEngagement(categoryId, pageable);
    }
    

    private Long getCategoryId(Artisan artisan) {
        return artisan.getCategory() != null ? artisan.getCategory().getId() : null;
    }
    

    private List<ArtisanInboxItemResponse> transformToInboxItems(List<ServiceRequest> serviceRequests, 
                                                                 Artisan artisan) {
        return serviceRequests.stream()
                .map(request -> createInboxItem(request, artisan))
                .collect(Collectors.toList());
    }

    private ArtisanInboxItemResponse createInboxItem(ServiceRequest serviceRequest, Artisan artisan) {
        String responseStatus = determineResponseStatus(serviceRequest, artisan);
        Long categoryId = extractCategoryId(serviceRequest);
        
        return new ArtisanInboxItemResponse(
                serviceRequest.getId(),
                serviceRequest.getTitle(),
                serviceRequest.getCity(),
                serviceRequest.getDescription(),
                serviceRequest.getDesiredDate(),
                serviceRequest.getCreatedAt(),
                responseStatus,
                categoryId
        );
    }
    

    private String determineResponseStatus(ServiceRequest serviceRequest, Artisan artisan) {
        boolean hasQuoted = quoteRepository.existsByRequest_IdAndArtisan_Id(
            serviceRequest.getId(), 
            artisan.getId()
        );
        
        return hasQuoted ? "RESPONDED" : "PENDING";
    }
    

    private Long extractCategoryId(ServiceRequest serviceRequest) {
        return serviceRequest.getCategory() != null ? serviceRequest.getCategory().getId() : null;
    }


    private List<ArtisanInboxItemResponse> applyFilters(List<ArtisanInboxItemResponse> items, 
                                                       String statusFilter, 
                                                       String searchQuery) {
        return items.stream()
                .filter(item -> matchesStatusFilter(item, statusFilter))
                .filter(item -> matchesSearchQuery(item, searchQuery))
                .collect(Collectors.toList());
    }
    

    private boolean matchesStatusFilter(ArtisanInboxItemResponse item, String statusFilter) {
        return isBlankOrAll(statusFilter) || item.getStatus().equalsIgnoreCase(statusFilter);
    }
    

    private boolean matchesSearchQuery(ArtisanInboxItemResponse item, String searchQuery) {
        if (isBlank(searchQuery)) {
            return true;
        }
        
        String lowercaseQuery = searchQuery.toLowerCase().trim();
        
        return containsIgnoreCase(item.getTitle(), lowercaseQuery) ||
               containsIgnoreCase(item.getCity(), lowercaseQuery) ||
               containsIgnoreCase(item.getDescription(), lowercaseQuery);
    }
    

    private boolean isBlankOrAll(String value) {
        return isBlank(value) || "ALL".equalsIgnoreCase(value.trim());
    }
    

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    

    private boolean containsIgnoreCase(String text, String searchTerm) {
        return text != null && text.toLowerCase().contains(searchTerm);
    }
}
