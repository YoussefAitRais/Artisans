package org.event.backend.repository;

import org.event.backend.entity.Media;
import org.event.backend.entity.MediaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {

    // Public portfolio
    List<Media> findByArtisan_IdAndIsPublicTrueAndStatusOrderByCreatedAtDesc(
            Long artisanId, MediaStatus status);

    // Owner view (all statuses)
    List<Media> findByArtisan_IdOrderByCreatedAtDesc(Long artisanId);

    Optional<Media> findByIdAndArtisan_Id(Long id, Long artisanId);

    boolean existsByArtisan_IdAndIsCoverTrue(Long artisanId);

    @Modifying
    @Query("update Media m set m.isCover = false where m.artisan.id = :artisanId and m.isCover = true")
    void clearCoverForArtisan(Long artisanId);
}
