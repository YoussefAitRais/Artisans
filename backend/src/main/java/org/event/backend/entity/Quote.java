package org.event.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Quote (devis) proposed by an Artisan for a ServiceRequest.
 */
@Entity
@Table(name = "quotes", indexes = {
        @Index(name = "idx_quotes_request", columnList = "request_id"),
        @Index(name = "idx_quotes_artisan", columnList = "artisan_id"),
        @Index(name = "idx_quotes_status", columnList = "status")
})
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The request this quote belongs to
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_quote_request"))
    private ServiceRequest request;

    // The artisan who sent this quote
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_quote_artisan"))
    private Artisan artisan;

    // Monetary amount (use BigDecimal for money)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // Estimated number of days to complete the job
    @Column
    private Integer estimatedDays;

    // Optional message to the client
    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuoteStatus status = QuoteStatus.SENT;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = QuoteStatus.SENT;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // ---------- Getters / Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ServiceRequest getRequest() { return request; }
    public void setRequest(ServiceRequest request) { this.request = request; }

    public Artisan getArtisan() { return artisan; }
    public void setArtisan(Artisan artisan) { this.artisan = artisan; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public QuoteStatus getStatus() { return status; }
    public void setStatus(QuoteStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
