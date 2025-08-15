package org.event.backend.service.messaging;

import org.event.backend.dto.messaging.ConversationResponse;
import org.event.backend.dto.messaging.MessageCreateRequest;
import org.event.backend.dto.messaging.MessageResponse;
import org.event.backend.entity.*;
import org.event.backend.repository.ConversationRepository;
import org.event.backend.repository.EngagementRepository;
import org.event.backend.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business logic for conversations & messages. */
@Service
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final EngagementRepository engagementRepository;
    private final MessageRepository messageRepository;

    public MessagingService(ConversationRepository conversationRepository,
                            EngagementRepository engagementRepository,
                            MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.engagementRepository = engagementRepository;
        this.messageRepository = messageRepository;
    }

    // ---------- Conversations ----------

    @Transactional(readOnly = true)
    public Page<ConversationResponse> myConversations(Utilisateur current, Pageable pageable) {
        if (current instanceof Client c) {
            return conversationRepository.findByClient_IdOrArtisan_Id(c.getId(), -1L, pageable)
                    .map(this::toConversationDto);
        } else if (current instanceof Artisan a) {
            return conversationRepository.findByClient_IdOrArtisan_Id(-1L, a.getId(), pageable)
                    .map(this::toConversationDto);
        }
        throw new IllegalStateException("Only CLIENT or ARTISAN can list conversations");
    }

    /** Get or create a conversation for an engagement, ensuring the caller is a participant. */
    @Transactional
    public ConversationResponse getOrCreateByEngagement(Utilisateur current, Long engagementId) {
        Engagement e = engagementRepository.findById(engagementId)
                .orElseThrow(() -> new IllegalArgumentException("Engagement not found"));

        // Ownership check
        if (!isParticipant(current, e)) {
            throw new IllegalStateException("Not allowed to access this engagement conversation");
        }

        Conversation conv = conversationRepository.findByEngagement_Id(engagementId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setEngagement(e);
                    c.setClient(e.getClient());
                    c.setArtisan(e.getArtisan());
                    return conversationRepository.save(c);
                });

        return toConversationDto(conv);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(Utilisateur current, Long conversationId) {
        Conversation conv = fetchOwnedConversation(current, conversationId);
        return toConversationDto(conv);
    }

    // ---------- Messages ----------

    @Transactional(readOnly = true)
    public Page<MessageResponse> listMessages(Utilisateur current, Long conversationId, Pageable pageable) {
        fetchOwnedConversation(current, conversationId); // authorization
        return messageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::toMessageDto);
    }

    @Transactional
    public MessageResponse sendMessage(Utilisateur current, Long conversationId, MessageCreateRequest req) {
        Conversation conv = fetchOwnedConversation(current, conversationId);

        MessageSenderRole role = resolveSenderRole(current, conv);

        Message m = new Message();
        m.setConversation(conv);
        m.setSenderRole(role);
        m.setBody(req.getBody());

        return toMessageDto(messageRepository.save(m));
    }

    // ---------- Helpers ----------

    private Conversation fetchOwnedConversation(Utilisateur current, Long id) {
        if (current instanceof Client c) {
            return conversationRepository.findByIdAndClient_Id(id, c.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        } else if (current instanceof Artisan a) {
            return conversationRepository.findByIdAndArtisan_Id(id, a.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        }
        throw new IllegalStateException("Only CLIENT or ARTISAN can access conversations");
    }

    private boolean isParticipant(Utilisateur current, Engagement e) {
        if (current instanceof Client c) {
            return e.getClient().getId().equals(c.getId());
        } else if (current instanceof Artisan a) {
            return e.getArtisan().getId().equals(a.getId());
        }
        return false;
    }

    private MessageSenderRole resolveSenderRole(Utilisateur current, Conversation conv) {
        if (current instanceof Client c && conv.getClient().getId().equals(c.getId())) {
            return MessageSenderRole.CLIENT;
        }
        if (current instanceof Artisan a && conv.getArtisan().getId().equals(a.getId())) {
            return MessageSenderRole.ARTISAN;
        }
        throw new IllegalStateException("User is not a participant of this conversation");
    }

    private ConversationResponse toConversationDto(Conversation c) {
        return new ConversationResponse(
                c.getId(),
                c.getEngagement().getId(),
                c.getClient().getId(),
                c.getArtisan().getId(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    private MessageResponse toMessageDto(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getConversation().getId(),
                m.getSenderRole(),
                m.getBody(),
                m.getCreatedAt()
        );
    }
}
