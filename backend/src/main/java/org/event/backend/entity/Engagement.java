package org.event.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "engagements",
        uniqueConstraints = {
                // one engagement per request (optional but recommended)
                @UniqueConstraint(name = "uk_engagement_request", columnNames = {"request_id"}),
                // and one engagement per accepted quote
                @UniqueConstraint(name = "uk_engagement_quote", columnNames = {"quote_id"})
        },
        indexes = {
                @Index(name = "idx_engagement_client",  columnList = "client_id"),
                @Index(name = "idx_engagement_artisan", columnList = "artisan_id"),
                @Index(name = "idx_engagement_status",  columnList = "status")
        }
)
public class Engagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Accepted request
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_engagement_request"))
    private ServiceRequest request;

    // Accepted quote
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_engagement_quote"))
    private Quote quote;

    // Actors
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_engagement_client"))
    private Client client;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_engagement_artisan"))
    private Artisan artisan;

    // Price agreed on acceptance time
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal agreedPrice;

    // Schedule (optional until artisan confirms)
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EngagementStatus status = EngagementStatus.PENDING_CONFIRMATION;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = EngagementStatus.PENDING_CONFIRMATION;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // ---- Getters/Setters ----
    public Long getId() { return id; }
    public ServiceRequest getRequest() { return request; }
    public void setRequest(ServiceRequest request) { this.request = request; }
    public Quote getQuote() { return quote; }
    public void setQuote(Quote quote) { this.quote = quote; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Artisan getArtisan() { return artisan; }
    public void setArtisan(Artisan artisan) { this.artisan = artisan; }
    public BigDecimal getAgreedPrice() { return agreedPrice; }
    public void setAgreedPrice(BigDecimal agreedPrice) { this.agreedPrice = agreedPrice; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public EngagementStatus getStatus() { return status; }
    public void setStatus(EngagementStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
