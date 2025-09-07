package org.event.backend.repository;

import org.event.backend.entity.Engagement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngagementRepository extends JpaRepository<Engagement, Long> {
    boolean existsByRequest_Id(Long requestId);
}
