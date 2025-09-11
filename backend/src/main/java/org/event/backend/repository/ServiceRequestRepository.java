package org.event.backend.repository;

import org.event.backend.entity.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;


public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {


    Page<ServiceRequest> findByClient_Id(Long clientId, Pageable pageable);


    @Query("SELECT sr FROM ServiceRequest sr " +
           "WHERE (:categoryId IS NULL OR sr.category.id = :categoryId) " +
           "AND NOT EXISTS (SELECT 1 FROM Engagement e WHERE e.request.id = sr.id)")
    Page<ServiceRequest> findByCategoryWithoutEngagement(@Param("categoryId") Long categoryId, Pageable pageable);


    List<ServiceRequest> findByIdIn(List<Long> ids);


    Optional<ServiceRequest> findByIdAndClient_Id(Long id, Long clientId);
}
