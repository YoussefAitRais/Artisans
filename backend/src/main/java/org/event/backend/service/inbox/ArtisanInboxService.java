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

/**
 * Service for managing artisan inbox functionality.
 * 
 * Provides artisans with a filtered view of available service requests
 * that match their category and current response status. Supports
 * pagination, status filtering, and text search capabilities.
 * 
 * @author Artisan Platform Team
 * @version 1.0
 */
@Service
public class ArtisanInboxService {

    // ============ DEPENDENCIES ============
    
    private final ServiceRequestRepository serviceRequestRepository;
    private final QuoteRepository quoteRepository;

    /**
     * Constructor for dependency injection.
     * 
     * @param serviceRequestRepository repository for service request data access
     * @param quoteRepository repository for quote data access
     */
    public ArtisanInboxService(ServiceRequestRepository serviceRequestRepository, 
                               QuoteRepository quoteRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.quoteRepository = quoteRepository;
    }
    // ============ PUBLIC METHODS ============
    
    /**
     * Retrieves paginated inbox items for an artisan with optional filtering.
     * 
     * The inbox contains service requests that:
     * - Match the artisan's category
     * - Don't have existing engagements (still available)
     * - Can be filtered by response status and search query
     * 
     * @param artisan the artisan requesting their inbox
     * @param pageable pagination parameters
     * @param statusFilter filter by response status (optional)
     * @param searchQuery text search across title, city, description (optional)
     * @return paginated list of inbox items
     */
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
    
    /**
     * Retrieves service requests available for the artisan to quote on.
     * 
     * @param artisan the artisan requesting service requests
     * @param pageable pagination parameters
     * @return paginated service requests matching artisan's category without engagements
     */
    private Page<ServiceRequest> getAvailableServiceRequests(Artisan artisan, Pageable pageable) {
        Long categoryId = getCategoryId(artisan);
        return serviceRequestRepository.findByCategoryWithoutEngagement(categoryId, pageable);
    }
    
    /**
     * Safely extracts category ID from artisan, handling null cases.
     * 
     * @param artisan the artisan entity
     * @return category ID or null if not available
     */
    private Long getCategoryId(Artisan artisan) {
        return artisan.getCategory() != null ? artisan.getCategory().getId() : null;
    }
    
    /**
     * Transforms a list of service requests into inbox item responses.
     * 
     * @param serviceRequests list of service requests to transform
     * @param artisan the artisan for determining response status
     * @return list of inbox item responses
     */
    private List<ArtisanInboxItemResponse> transformToInboxItems(List<ServiceRequest> serviceRequests, 
                                                                 Artisan artisan) {
        return serviceRequests.stream()
                .map(request -> createInboxItem(request, artisan))
                .collect(Collectors.toList());
    }
    
    /**
     * Creates an inbox item response from a service request.
     * 
     * @param serviceRequest the source service request
     * @param artisan the artisan for determining response status
     * @return inbox item response with computed status
     */
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
    
    /**
     * Determines the response status for a service request from the artisan's perspective.
     * 
     * @param serviceRequest the service request to check
     * @param artisan the artisan checking their response status
     * @return "RESPONDED" if artisan has quoted, "PENDING" otherwise
     */
    private String determineResponseStatus(ServiceRequest serviceRequest, Artisan artisan) {
        boolean hasQuoted = quoteRepository.existsByRequest_IdAndArtisan_Id(
            serviceRequest.getId(), 
            artisan.getId()
        );
        
        return hasQuoted ? "RESPONDED" : "PENDING";
    }
    
    /**
     * Safely extracts category ID from service request.
     * 
     * @param serviceRequest the service request
     * @return category ID or null if not available
     */
    private Long extractCategoryId(ServiceRequest serviceRequest) {
        return serviceRequest.getCategory() != null ? serviceRequest.getCategory().getId() : null;
    }

    /**
     * Applies status and search filters to inbox items.
     * 
     * @param items the inbox items to filter
     * @param statusFilter status filter to apply (optional)
     * @param searchQuery search query to apply (optional)
     * @return filtered list of inbox items
     */
    private List<ArtisanInboxItemResponse> applyFilters(List<ArtisanInboxItemResponse> items, 
                                                       String statusFilter, 
                                                       String searchQuery) {
        return items.stream()
                .filter(item -> matchesStatusFilter(item, statusFilter))
                .filter(item -> matchesSearchQuery(item, searchQuery))
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if an inbox item matches the status filter.
     * 
     * @param item the inbox item to check
     * @param statusFilter the status filter (null or "ALL" means no filter)
     * @return true if item matches filter, false otherwise
     */
    private boolean matchesStatusFilter(ArtisanInboxItemResponse item, String statusFilter) {
        return isBlankOrAll(statusFilter) || item.getStatus().equalsIgnoreCase(statusFilter);
    }
    
    /**
     * Checks if an inbox item matches the search query.
     * 
     * @param item the inbox item to check
     * @param searchQuery the search query (null or blank means no filter)
     * @return true if item matches search, false otherwise
     */
    private boolean matchesSearchQuery(ArtisanInboxItemResponse item, String searchQuery) {
        if (isBlank(searchQuery)) {
            return true;
        }
        
        String lowercaseQuery = searchQuery.toLowerCase().trim();
        
        return containsIgnoreCase(item.getTitle(), lowercaseQuery) ||
               containsIgnoreCase(item.getCity(), lowercaseQuery) ||
               containsIgnoreCase(item.getDescription(), lowercaseQuery);
    }
    
    /**
     * Checks if a string is null, blank, or equals "ALL" (case-insensitive).
     * 
     * @param value the string to check
     * @return true if blank or "ALL", false otherwise
     */
    private boolean isBlankOrAll(String value) {
        return isBlank(value) || "ALL".equalsIgnoreCase(value.trim());
    }
    
    /**
     * Checks if a string is null or blank.
     * 
     * @param value the string to check
     * @return true if null or blank, false otherwise
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * Performs case-insensitive substring search, handling null values safely.
     * 
     * @param text the text to search in
     * @param searchTerm the term to search for
     * @return true if text contains search term (case-insensitive), false otherwise
     */
    private boolean containsIgnoreCase(String text, String searchTerm) {
        return text != null && text.toLowerCase().contains(searchTerm);
    }
}
