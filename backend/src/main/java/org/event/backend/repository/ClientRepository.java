package org.event.backend.repository;

import org.event.backend.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ClientRepository extends JpaRepository<Client, Long> {

    // Admin search across name/email/telephone (any match)
    Page<Client> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelephoneContainingIgnoreCase(
            String nom, String prenom, String email, String telephone, Pageable pageable
    );
}
