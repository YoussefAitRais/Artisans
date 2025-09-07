package org.event.backend.dto.quote;

import org.event.backend.entity.QuoteStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class QuoteResponse {
    private Long id;
    private Long requestId;
    private Long artisanId;
    private BigDecimal price;
    private Integer estimatedDays;
    private String message;
    private String status; // SENT | ACCEPTED | REJECTED
    private Instant createdAt;

    public QuoteResponse() {}

    public QuoteResponse(Long id,
                         Long requestId,
                         Long artisanId,
                         BigDecimal price,
                         Integer estimatedDays,
                         String message,
                         QuoteStatus status,
                         Instant createdAt) {
        this.id = id;
        this.requestId = requestId;
        this.artisanId = artisanId;
        this.price = price;
        this.estimatedDays = estimatedDays;
        this.message = message;
        this.status = status != null ? status.name() : null;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public Long getArtisanId() { return artisanId; }
    public BigDecimal getPrice() { return price; }
    public Integer getEstimatedDays() { return estimatedDays; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public void setArtisanId(Long artisanId) { this.artisanId = artisanId; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }
    public void setMessage(String message) { this.message = message; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
