package org.event.backend.dto.review;

import java.time.Instant;

/**
 * Response DTO for reviews.
 */
public class ReviewResponse {

    private Long id;
    private Long artisanId;
    private Long clientId;
    private Long requestId; // may be null
    private int rating;
    private String comment;
    private Instant createdAt;

    public ReviewResponse() {}

    public ReviewResponse(Long id, Long artisanId, Long clientId, Long requestId,
                          int rating, String comment, Instant createdAt) {
        this.id = id;
        this.artisanId = artisanId;
        this.clientId = clientId;
        this.requestId = requestId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArtisanId() { return artisanId; }
    public void setArtisanId(Long artisanId) { this.artisanId = artisanId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
