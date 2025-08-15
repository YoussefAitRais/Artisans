package org.event.backend.dto.engagement;

import org.event.backend.entity.EngagementStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Data sent to client/ artisan dashboards. */
public class EngagementResponse {

    private Long id;
    private Long requestId;
    private Long quoteId;
    private Long clientId;
    private Long artisanId;
    private BigDecimal agreedPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private EngagementStatus status;
    private Instant createdAt;

    public EngagementResponse() {}

    public EngagementResponse(Long id, Long requestId, Long quoteId, Long clientId, Long artisanId,
                              BigDecimal agreedPrice, LocalDate startDate, LocalDate endDate,
                              EngagementStatus status, Instant createdAt) {
        this.id = id;
        this.requestId = requestId;
        this.quoteId = quoteId;
        this.clientId = clientId;
        this.artisanId = artisanId;
        this.agreedPrice = agreedPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters/Setters ...
    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public Long getQuoteId() { return quoteId; }
    public Long getClientId() { return clientId; }
    public Long getArtisanId() { return artisanId; }
    public BigDecimal getAgreedPrice() { return agreedPrice; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public EngagementStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setArtisanId(Long artisanId) { this.artisanId = artisanId; }
    public void setAgreedPrice(BigDecimal agreedPrice) { this.agreedPrice = agreedPrice; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setStatus(EngagementStatus status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
