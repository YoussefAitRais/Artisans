package org.event.backend.repository;

import org.event.backend.entity.Engagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EngagementRepository extends JpaRepository<Engagement, Long> {

    Page<Engagement> findByClient_Id(Long clientId, Pageable pageable);

    Page<Engagement> findByArtisan_Id(Long artisanId, Pageable pageable);

    Optional<Engagement> findByIdAndClient_Id(Long id, Long clientId);

    Optional<Engagement> findByIdAndArtisan_Id(Long id, Long artisanId);

    boolean existsByRequest_Id(Long requestId);

    boolean existsByQuote_Id(Long quoteId);
}
