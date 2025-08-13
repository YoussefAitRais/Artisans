package org.event.backend.repository;

import org.event.backend.entity.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repository with helper finders + specifications for flexible filtering.
 */
public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {

    Page<ServiceRequest> findByClient_Id(Long clientId, Pageable pageable);

    Optional<ServiceRequest> findByIdAndClient_Id(Long id, Long clientId);
}
