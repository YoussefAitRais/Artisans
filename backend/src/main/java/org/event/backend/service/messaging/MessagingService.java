package org.event.backend.service.messaging;

import org.event.backend.dto.messaging.ConversationResponse;   // <-- التصحيح هنا
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
import org.springframework.util.StringUtils;


@Service
public class MessagingService {

    // === CONSTANTS: No magic numbers or strings ===
    private static final Long INVALID_ID = -1L;
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final String CONVERSATION_NOT_FOUND = "Conversation not found";
    private static final String ENGAGEMENT_NOT_FOUND = "Engagement not found";
    private static final String ACCESS_DENIED = "Not allowed to access this conversation";
    private static final String USER_NULL_ERROR = "User cannot be null";
    private static final String INVALID_USER_TYPE = "Only CLIENT or ARTISAN can access messaging";
    private static final String MESSAGE_REQUEST_NULL = "Message request cannot be null";
    private static final String MESSAGE_BODY_EMPTY = "Message body cannot be empty";
    private static final String MESSAGE_TOO_LONG = "Message body cannot exceed " + MAX_MESSAGE_LENGTH + " characters";
    private static final String ID_INVALID = " must be a positive number";

    // === DEPENDENCIES: Injected repositories ===
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

    // ========================================
    // CONVERSATION OPERATIONS
    // ========================================

    @Transactional(readOnly = true)
    public Page<ConversationResponse> myConversations(Utilisateur current, Pageable pageable) {
        validateUser(current);

        if (current instanceof Client client) {
            return findConversationsForClient(client.getId(), pageable);
        }
        if (current instanceof Artisan artisan) {
            return findConversationsForArtisan(artisan.getId(), pageable);
        }
        throw new IllegalStateException(INVALID_USER_TYPE);
    }

    private Page<ConversationResponse> findConversationsForClient(Long clientId, Pageable pageable) {
        return conversationRepository
                .findByClient_IdOrArtisan_Id(clientId, INVALID_ID, pageable)
                .map(this::mapToConversationResponse);
    }

    private Page<ConversationResponse> findConversationsForArtisan(Long artisanId, Pageable pageable) {
        return conversationRepository
                .findByClient_IdOrArtisan_Id(INVALID_ID, artisanId, pageable)
                .map(this::mapToConversationResponse);
    }

    @Transactional
    public ConversationResponse getOrCreateByEngagement(Utilisateur current, Long engagementId) {
        validateUser(current);
        validateId(engagementId, "Engagement ID");

        Engagement engagement = findEngagementById(engagementId);
        validateUserCanAccessEngagement(current, engagement);

        Conversation conversation = findOrCreateConversation(engagement);
        return mapToConversationResponse(conversation);
    }

    private Engagement findEngagementById(Long engagementId) {
        return engagementRepository.findById(engagementId)
                .orElseThrow(() -> new IllegalArgumentException(ENGAGEMENT_NOT_FOUND));
    }

    private Conversation findOrCreateConversation(Engagement engagement) {
        return conversationRepository.findByEngagement_Id(engagement.getId())
                .orElseGet(() -> createNewConversationForEngagement(engagement));
    }

    private Conversation createNewConversationForEngagement(Engagement engagement) {
        Conversation c = new Conversation();
        c.setEngagement(engagement);
        c.setClient(engagement.getClient());
        c.setArtisan(engagement.getArtisan());
        return conversationRepository.save(c);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(Utilisateur current, Long conversationId) {
        validateUser(current);
        validateId(conversationId, "Conversation ID");

        Conversation conversation = fetchUserOwnedConversation(current, conversationId);
        return mapToConversationResponse(conversation);
    }

    // MESSAGE OPERATIONS

    @Transactional(readOnly = true)
    public Page<MessageResponse> listMessages(Utilisateur current, Long conversationId, Pageable pageable) {
        validateUser(current);
        validateId(conversationId, "Conversation ID");

        // Ensure access
        fetchUserOwnedConversation(current, conversationId);

        return messageRepository
                .findByConversation_IdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::mapToMessageResponse);
    }

    //Sends a message in a conversation
    @Transactional
    public MessageResponse sendMessage(Utilisateur current, Long conversationId, MessageCreateRequest request) {
        validateUser(current);
        validateId(conversationId, "Conversation ID");
        validateMessageRequest(request);

        Conversation conversation = fetchUserOwnedConversation(current, conversationId);
        MessageSenderRole senderRole = determineSenderRole(current, conversation);

        Message newMessage = buildMessage(conversation, senderRole, request.getBody());
        Message saved = messageRepository.save(newMessage);
        return mapToMessageResponse(saved);
    }

    private Message buildMessage(Conversation conversation, MessageSenderRole senderRole, String messageBody) {
        Message m = new Message();
        m.setConversation(conversation);
        m.setSenderRole(senderRole);
        m.setBody(messageBody.trim());
        return m;
    }

    // VALIDATION

    private void validateUser(Utilisateur user) {
        if (user == null) throw new IllegalArgumentException(USER_NULL_ERROR);
        boolean ok = (user instanceof Client) || (user instanceof Artisan);
        if (!ok) throw new IllegalStateException(INVALID_USER_TYPE);
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) throw new IllegalArgumentException(fieldName + ID_INVALID);
    }

    private void validateMessageRequest(MessageCreateRequest request) {
        if (request == null) throw new IllegalArgumentException(MESSAGE_REQUEST_NULL);
        if (!StringUtils.hasText(request.getBody())) throw new IllegalArgumentException(MESSAGE_BODY_EMPTY);
        if (request.getBody().trim().length() > MAX_MESSAGE_LENGTH) throw new IllegalArgumentException(MESSAGE_TOO_LONG);
    }

    private void validateUserCanAccessEngagement(Utilisateur user, Engagement engagement) {
        if (!isUserPartOfEngagement(user, engagement)) throw new IllegalStateException(ACCESS_DENIED);
    }

    // BUSINESS HELPERS

    private Conversation fetchUserOwnedConversation(Utilisateur current, Long conversationId) {
        if (current instanceof Client client) {
            return findConversationForSpecificClient(conversationId, client.getId());
        }
        if (current instanceof Artisan artisan) {
            return findConversationForSpecificArtisan(conversationId, artisan.getId());
        }
        throw new IllegalStateException(INVALID_USER_TYPE);
    }

    private Conversation findConversationForSpecificClient(Long conversationId, Long clientId) {
        return conversationRepository.findByIdAndClient_Id(conversationId, clientId)
                .orElseThrow(() -> new IllegalArgumentException(CONVERSATION_NOT_FOUND));
    }

    private Conversation findConversationForSpecificArtisan(Long conversationId, Long artisanId) {
        return conversationRepository.findByIdAndArtisan_Id(conversationId, artisanId)
                .orElseThrow(() -> new IllegalArgumentException(CONVERSATION_NOT_FOUND));
    }

    private boolean isUserPartOfEngagement(Utilisateur user, Engagement engagement) {
        if (user instanceof Client client) {
            return engagement.getClient().getId().equals(client.getId());
        }
        if (user instanceof Artisan artisan) {
            return engagement.getArtisan().getId().equals(artisan.getId());
        }
        return false;
    }

    private MessageSenderRole determineSenderRole(Utilisateur user, Conversation conversation) {
        if (user instanceof Client client && conversation.getClient().getId().equals(client.getId())) {
            return MessageSenderRole.CLIENT;
        }
        if (user instanceof Artisan artisan && conversation.getArtisan().getId().equals(artisan.getId())) {
            return MessageSenderRole.ARTISAN;
        }
        throw new IllegalStateException("User is not a participant of this conversation");
    }

    // MAPPERS

    private ConversationResponse mapToConversationResponse(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getEngagement().getId(),
                conversation.getClient().getId(),
                conversation.getArtisan().getId(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private MessageResponse mapToMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSenderRole(),
                message.getBody(),
                message.getCreatedAt()
        );
    }
}
