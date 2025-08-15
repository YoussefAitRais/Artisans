package org.event.backend.repository;

import org.event.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversation_IdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);
}
