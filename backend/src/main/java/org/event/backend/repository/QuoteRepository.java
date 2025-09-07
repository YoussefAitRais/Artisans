package org.event.backend.repository;

import org.event.backend.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    boolean existsByRequest_IdAndArtisan_Id(Long requestId, Long artisanId);

    Page<Quote> findByArtisan_Id(Long artisanId, Pageable pageable);

    List<Quote> findByRequest_Id(Long requestId);

    Optional<Quote> findByIdAndArtisan_Id(Long id, Long artisanId); // <-- ضروري
}
