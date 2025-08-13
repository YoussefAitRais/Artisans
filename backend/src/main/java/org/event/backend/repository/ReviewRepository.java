package org.event.backend.repository;

import org.event.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByArtisan_Id(Long artisanId, Pageable pageable);

    Page<Review> findByClient_Id(Long clientId, Pageable pageable);

    boolean existsByClient_IdAndRequest_Id(Long clientId, Long requestId);

    @Query("select avg(r.rating) from Review r where r.artisan.id = :artisanId")
    Double getAverageRatingByArtisanId(Long artisanId);
}
