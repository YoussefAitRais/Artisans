package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.messaging.ConversationResponse;
import org.event.backend.dto.messaging.MessageCreateRequest;
import org.event.backend.dto.messaging.MessageResponse;
import org.event.backend.entity.Utilisateur;
import org.event.backend.service.messaging.MessagingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST endpoints for conversations and messages. */
@RestController
@RequestMapping("/api/conversations")
public class MessagingController {

    private final MessagingService messagingService;

    public MessagingController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    // List my conversations (client or artisan)
    @GetMapping("/my")
    public ResponseEntity<Page<ConversationResponse>> myConversations(
            @AuthenticationPrincipal Utilisateur current,
            Pageable pageable
    ) {
        return ResponseEntity.ok(messagingService.myConversations(current, pageable));
    }

    // Get or create conversation for an engagement (idempotent create)
    @PostMapping("/by-engagement/{engagementId}")
    public ResponseEntity<ConversationResponse> getOrCreateByEngagement(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable Long engagementId
    ) {
        return ResponseEntity.ok(messagingService.getOrCreateByEngagement(current, engagementId));
    }

    // Get conversation details
    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getConversation(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(messagingService.getConversation(current, id));
    }

    // List messages in a conversation
    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<MessageResponse>> listMessages(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable Long id,
            Pageable pageable
    ) {
        return ResponseEntity.ok(messagingService.listMessages(current, id, pageable));
    }

    // Send a message in a conversation
    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal Utilisateur current,
            @PathVariable Long id,
            @Valid @RequestBody MessageCreateRequest req
    ) {
        return ResponseEntity.ok(messagingService.sendMessage(current, id, req));
    }
}
