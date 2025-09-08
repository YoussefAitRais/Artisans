package org.event.backend.dto.messaging;

import java.time.Instant;

/** Minimal conversation data for lists/details. */
public class ConversationResponse {
    private Long id;
    private Long engagementId;
    private Long clientId;
    private Long artisanId;
    private Instant createdAt;
    private Instant updatedAt;

    public ConversationResponse() {}

    public ConversationResponse(Long id, Long engagementId, Long clientId, Long artisanId,
                                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.engagementId = engagementId;
        this.clientId = clientId;
        this.artisanId = artisanId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getEngagementId() { return engagementId; }
    public Long getClientId() { return clientId; }
    public Long getArtisanId() { return artisanId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setId(Long id) { this.id = id; }
    public void setEngagementId(Long engagementId) { this.engagementId = engagementId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setArtisanId(Long artisanId) { this.artisanId = artisanId; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
