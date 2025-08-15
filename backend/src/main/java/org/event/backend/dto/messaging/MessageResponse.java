package org.event.backend.dto.messaging;

import org.event.backend.entity.MessageSenderRole;
import java.time.Instant;

/** Message data returned to clients. */
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private MessageSenderRole senderRole;
    private String body;
    private Instant createdAt;

    public MessageResponse() {}

    public MessageResponse(Long id, Long conversationId, MessageSenderRole senderRole,
                           String body, Instant createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderRole = senderRole;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public MessageSenderRole getSenderRole() { return senderRole; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public void setSenderRole(MessageSenderRole senderRole) { this.senderRole = senderRole; }
    public void setBody(String body) { this.body = body; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
