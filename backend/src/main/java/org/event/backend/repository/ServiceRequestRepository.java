package org.event.backend.repository;

import org.event.backend.entity.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {

    Page<ServiceRequest> findByClient_Id(Long clientId, Pageable pageable);

    Page<ServiceRequest> findByCategory_IdAndEngagementIsNull(Long categoryId, Pageable pageable);

    List<ServiceRequest> findByIdIn(List<Long> ids);

    Optional<ServiceRequest> findByIdAndClient_Id(Long id, Long clientId); // <-- ضروري
}
