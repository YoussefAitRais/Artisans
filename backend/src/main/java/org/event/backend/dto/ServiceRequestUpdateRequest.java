package org.event.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Payload to update a service request by its owner while PENDING.
 */
public class ServiceRequestUpdateRequest {

    private Long categoryId; // optional

    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 120)
    private String city;

    @Size(max = 2000)
    private String description;

    private LocalDate desiredDate;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDesiredDate() { return desiredDate; }
    public void setDesiredDate(LocalDate desiredDate) { this.desiredDate = desiredDate; }
}
