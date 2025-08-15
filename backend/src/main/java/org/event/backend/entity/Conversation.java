package org.event.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** One conversation per engagement (client <-> artisan). */
@Entity
@Table(name = "conversations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_conversation_engagement", columnNames = {"engagement_id"})
        },
        indexes = {
                @Index(name = "idx_conversation_client", columnList = "client_id"),
                @Index(name = "idx_conversation_artisan", columnList = "artisan_id")
        })
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Conversation is bound to one engagement
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "engagement_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_conversation_engagement"))
    private Engagement engagement;

    // Participants (duplicated for quick access/authorization)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_conversation_client"))
    private Client client;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_conversation_artisan"))
    private Artisan artisan;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters/Setters
    public Long getId() { return id; }
    public Engagement getEngagement() { return engagement; }
    public void setEngagement(Engagement engagement) { this.engagement = engagement; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Artisan getArtisan() { return artisan; }
    public void setArtisan(Artisan artisan) { this.artisan = artisan; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
