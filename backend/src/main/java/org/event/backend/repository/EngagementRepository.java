package org.event.backend.repository;

import org.event.backend.entity.Engagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EngagementRepository extends JpaRepository<Engagement, Long> {
    boolean existsByRequest_Id(Long requestId);
    
    // Find engagements for a specific client with pagination
    Page<Engagement> findByClient_Id(Long clientId, Pageable pageable);
    
    // Find engagements for a specific artisan with pagination
    Page<Engagement> findByArtisan_Id(Long artisanId, Pageable pageable);
    
    // Find engagement by ID and artisan (for artisan-specific operations)
    Optional<Engagement> findByIdAndArtisan_Id(Long id, Long artisanId);
    
    // Find engagement by ID and client (for client-specific operations)
    Optional<Engagement> findByIdAndClient_Id(Long id, Long clientId);
}
