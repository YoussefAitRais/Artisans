package org.event.backend.repository;

import org.event.backend.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByEngagement_Id(Long engagementId);

    Page<Conversation> findByClient_IdOrArtisan_Id(Long clientId, Long artisanId, Pageable pageable);

    Optional<Conversation> findByIdAndClient_Id(Long id, Long clientId);

    Optional<Conversation> findByIdAndArtisan_Id(Long id, Long artisanId);
}
