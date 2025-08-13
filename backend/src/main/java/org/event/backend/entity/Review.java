package org.event.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A review left by a Client for an Artisan.
 * Optionally linked to a ServiceRequest (e.g., after completion).
 */
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_artisan", columnList = "artisan_id"),
        @Index(name = "idx_reviews_client", columnList = "client_id")
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reviewer (owner)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_client"))
    private Client client;

    // Reviewed artisan
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_artisan"))
    private Artisan artisan;

    // Optional link to a completed request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id",
            foreignKey = @ForeignKey(name = "fk_review_request"))
    private ServiceRequest request;

    @Column(nullable = false)
    private int rating; // 1..5

    @Column(length = 500)
    private String comment;

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

    // -------- Getters/Setters --------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Artisan getArtisan() { return artisan; }
    public void setArtisan(Artisan artisan) { this.artisan = artisan; }

    public ServiceRequest getRequest() { return request; }
    public void setRequest(ServiceRequest request) { this.request = request; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
