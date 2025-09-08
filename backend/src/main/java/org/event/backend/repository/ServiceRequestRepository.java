package org.event.backend.repository;

import org.event.backend.entity.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ServiceRequest entities - Clean Code for Beginners
 * 
 * This repository demonstrates:
 * - Simple method naming conventions
 * - Custom JPQL queries when Spring Data naming isn't sufficient
 * - Clear documentation for each method's purpose
 */
public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {

    /**
     * Finds all service requests created by a specific client
     * 
     * @param clientId The ID of the client
     * @param pageable Pagination settings
     * @return Page of service requests for this client
     */
    Page<ServiceRequest> findByClient_Id(Long clientId, Pageable pageable);

    /**
     * Finds service requests by category that don't have an engagement yet
     * 
     * This method uses a custom JPQL query because there's no direct 
     * relationship from ServiceRequest to Engagement in the entity model.
     * An engagement references a ServiceRequest, but not vice versa.
     * 
     * @param categoryId The category to filter by
     * @param pageable Pagination settings  
     * @return Page of service requests without engagements
     */
    @Query("SELECT sr FROM ServiceRequest sr " +
           "WHERE (:categoryId IS NULL OR sr.category.id = :categoryId) " +
           "AND NOT EXISTS (SELECT 1 FROM Engagement e WHERE e.request.id = sr.id)")
    Page<ServiceRequest> findByCategoryWithoutEngagement(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Finds multiple service requests by their IDs
     * 
     * Useful for batch operations or when you have a list of IDs to fetch.
     * 
     * @param ids List of service request IDs
     * @return List of matching service requests
     */
    List<ServiceRequest> findByIdIn(List<Long> ids);

    /**
     * Finds a service request by ID that belongs to a specific client
     * 
     * This provides security by ensuring clients can only access their own requests.
     * 
     * @param id The service request ID
     * @param clientId The client's ID (for ownership verification)
     * @return Optional containing the service request if found and owned by client
     */
    Optional<ServiceRequest> findByIdAndClient_Id(Long id, Long clientId);
}
