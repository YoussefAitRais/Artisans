package org.event.backend.dto.quote;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class QuoteCreateRequest {

    @NotNull
    @DecimalMin(value = "0.00", message = "Price must be >= 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @Min(value = 1, message = "Estimated days must be >= 1")
    private Integer estimatedDays; // optional but validated if present

    @Size(max = 1000)
    private String message;

    public QuoteCreateRequest() {}

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
