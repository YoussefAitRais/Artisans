package org.event.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/** A single message inside a conversation. */
@Entity
@Table(name = "messages",
        indexes = {
                @Index(name = "idx_message_conversation", columnList = "conversation_id"),
                @Index(name = "idx_message_created", columnList = "createdAt")
        })
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent conversation
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_message_conversation"))
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageSenderRole senderRole; // CLIENT | ARTISAN

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    // Getters/Setters
    public Long getId() { return id; }
    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    public MessageSenderRole getSenderRole() { return senderRole; }
    public void setSenderRole(MessageSenderRole senderRole) { this.senderRole = senderRole; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Instant getCreatedAt() { return createdAt; }
}
