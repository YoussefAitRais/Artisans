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
 * ArtisanInboxService - Clean Code for Beginners
 * 
 * This service handles the artisan's inbox, showing service requests 
 * that are relevant to the artisan's category and don't have 
 * engagements yet (i.e., still open for quotes).
 * 
 * Key concepts for beginners:
 * - Repository pattern: Use repositories to access data
 * - Stream API: Process collections functionally 
 * - Filtering: Apply business rules to filter results
 * - Pagination: Handle large datasets efficiently
 */
@Service
public class ArtisanInboxService {

    private final ServiceRequestRepository requestRepo;
    private final QuoteRepository quoteRepo;

    /**
     * Constructor injection - Spring automatically provides these repositories
     */
    public ArtisanInboxService(ServiceRequestRepository requestRepo, QuoteRepository quoteRepo) {
        this.requestRepo = requestRepo; 
        this.quoteRepo = quoteRepo;
    }

    /**
     * Lists service requests in the artisan's inbox
     * 
     * This method demonstrates clean code for beginners:
     * 1. Get requests that match artisan's category and have no engagement
     * 2. Transform each request into a response DTO
     * 3. Apply filters for status and search query
     * 4. Return paginated results
     * 
     * @param artisan The artisan requesting their inbox
     * @param pageable Pagination settings (page, size, sort)
     * @param status Filter by response status ("PENDING", "RESPONDED", or "ALL")
     * @param searchQuery Filter by text search in title, city, or description
     * @return Page of inbox items for this artisan
     */
    public Page<ArtisanInboxItemResponse> listInbox(Artisan artisan,
                                                    Pageable pageable,
                                                    String status, 
                                                    String searchQuery) {
        // Step 1: Get the artisan's category to filter relevant requests
        Long categoryId = artisan.getCategory().getId();

        // Step 2: Fetch service requests that:
        // - Match the artisan's category (or all if category is null)
        // - Don't have an engagement yet (still available for quotes)
        Page<ServiceRequest> serviceRequestPage = requestRepo.findByCategoryWithoutEngagement(categoryId, pageable);

        // Step 3: Transform each ServiceRequest into ArtisanInboxItemResponse
        List<ArtisanInboxItemResponse> transformedItems = serviceRequestPage.getContent().stream()
                .map(serviceRequest -> transformToInboxItem(serviceRequest, artisan))
                .collect(Collectors.toList());

        // Step 4: Apply additional filters (status and search)
        List<ArtisanInboxItemResponse> filteredItems = applyFilters(transformedItems, status, searchQuery);

        // Step 5: Return paginated results
        return new PageImpl<>(filteredItems, pageable, serviceRequestPage.getTotalElements());
    }
    
    /**
     * Transforms a ServiceRequest entity into an ArtisanInboxItemResponse DTO
     * 
     * This method demonstrates the transformation pattern for beginners:
     * - Take entity data (from database)
     * - Add computed fields (like response status)
     * - Create DTO (for API response)
     * 
     * @param serviceRequest The service request from database
     * @param artisan The artisan viewing the inbox (to check if they responded)
     * @return DTO with all needed information for the inbox
     */
    private ArtisanInboxItemResponse transformToInboxItem(ServiceRequest serviceRequest, Artisan artisan) {
        // Check if this artisan has already sent a quote for this request
        boolean hasRespondedWithQuote = quoteRepo.existsByRequest_IdAndArtisan_Id(
            serviceRequest.getId(), 
            artisan.getId()
        );
        
        // Determine the status from the artisan's perspective
        String responseStatus = hasRespondedWithQuote ? "RESPONDED" : "PENDING";
        
        // Create and return the response DTO
        return new ArtisanInboxItemResponse(
                serviceRequest.getId(),
                serviceRequest.getTitle(),
                serviceRequest.getCity(),
                serviceRequest.getDescription(),
                serviceRequest.getDesiredDate(),
                serviceRequest.getCreatedAt(),
                responseStatus,
                serviceRequest.getCategory() != null ? serviceRequest.getCategory().getId() : null
        );
    }

    /**
     * Applies status and search filters to inbox items
     * 
     * This method demonstrates filtering patterns for beginners:
     * - Use stream API for functional programming
     * - Apply multiple filters in sequence
     * - Handle null/empty values safely
     * 
     * @param items List of inbox items to filter
     * @param status Status filter ("PENDING", "RESPONDED", "ALL", or null)
     * @param searchQuery Text search filter (searches title, city, description)
     * @return Filtered list of inbox items
     */
    private List<ArtisanInboxItemResponse> applyFilters(List<ArtisanInboxItemResponse> items, 
                                                       String status, 
                                                       String searchQuery) {
        return items.stream()
                .filter(item -> matchesStatusFilter(item, status))
                .filter(item -> matchesSearchFilter(item, searchQuery))
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if an inbox item matches the status filter
     * 
     * @param item The inbox item to check
     * @param status The status filter ("PENDING", "RESPONDED", "ALL", or null)
     * @return true if item matches the filter
     */
    private boolean matchesStatusFilter(ArtisanInboxItemResponse item, String status) {
        // If no status filter or "ALL", include all items
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return true;
        }
        
        // Check if item status matches the filter
        return item.getStatus().equalsIgnoreCase(status);
    }
    
    /**
     * Checks if an inbox item matches the search query filter
     * 
     * @param item The inbox item to check  
     * @param searchQuery The search text (searches title, city, description)
     * @return true if item matches the search query
     */
    private boolean matchesSearchFilter(ArtisanInboxItemResponse item, String searchQuery) {
        // If no search query, include all items
        if (searchQuery == null || searchQuery.isBlank()) {
            return true;
        }
        
        // Search in title, city, or description (case-insensitive)
        return containsIgnoreCase(item.getTitle(), searchQuery) ||
               containsIgnoreCase(item.getCity(), searchQuery) ||
               containsIgnoreCase(item.getDescription(), searchQuery);
    }

    /**
     * Helper method for case-insensitive string searching
     * 
     * This method safely handles null values and performs case-insensitive search.
     * It's a common pattern in text filtering.
     * 
     * @param text The text to search in (can be null)
     * @param searchQuery The query to search for
     * @return true if searchQuery is found in text (case-insensitive)
     */
    private boolean containsIgnoreCase(String text, String searchQuery) {
        // Handle null text safely
        if (text == null) {
            return false;
        }
        
        // Perform case-insensitive search
        return text.toLowerCase().contains(searchQuery.toLowerCase().trim());
    }
}
