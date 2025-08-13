package org.event.backend.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload to create a review by a client.
 */
public class ReviewCreateRequest {

    @NotNull
    private Long artisanId;

    // optional, if you want to link to a completed request
    private Long requestId;

    @Min(1) @Max(5)
    private int rating;

    @Size(max = 500)
    private String comment;

    public Long getArtisanId() { return artisanId; }
    public void setArtisanId(Long artisanId) { this.artisanId = artisanId; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
