package org.event.backend.dto.quote;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Payload for updating a quote while it's still SENT.
 */
public class QuoteUpdateRequest {

    @DecimalMin(value = "0.00", message = "Price must be >= 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    private Integer estimatedDays;

    @Size(max = 1000)
    private String message;

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
