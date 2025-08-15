package org.event.backend.dto.quote;

import org.event.backend.entity.QuoteStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Quote data returned to clients/admins.
 */
public class QuoteResponse {

    private Long id;
    private Long requestId;
    private Long artisanId;
    private BigDecimal price;
    private Integer estimatedDays;
    private String message;
    private QuoteStatus status;
    private Instant createdAt;

    public QuoteResponse() {}

    public QuoteResponse(Long id, Long requestId, Long artisanId,
                         BigDecimal price, Integer estimatedDays,
                         String message, QuoteStatus status, Instant createdAt) {
        this.id = id;
        this.requestId = requestId;
        this.artisanId = artisanId;
        this.price = price;
        this.estimatedDays = estimatedDays;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Long getArtisanId() { return artisanId; }
    public void setArtisanId(Long artisanId) { this.artisanId = artisanId; }
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
}
