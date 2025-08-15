package org.event.backend.dto.quote;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Payload sent by an artisan to create a quote.
 */
public class QuoteCreateRequest {

    @NotNull
    @DecimalMin(value = "0.00", message = "Price must be >= 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    // optional but if provided it must be >= 1
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
