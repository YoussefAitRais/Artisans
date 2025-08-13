package org.event.backend.repository;

import org.event.backend.entity.Artisan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface ArtisanRepository extends JpaRepository<Artisan, Long>, JpaSpecificationExecutor<Artisan> {
}
