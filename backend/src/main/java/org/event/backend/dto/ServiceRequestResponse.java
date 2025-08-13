package org.event.backend.dto;

import org.event.backend.entity.RequestStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for returning service request data.
 */
public class ServiceRequestResponse {

    private Long id;
    private Long categoryId;
    private String title;
    private String city;
    private String description;
    private LocalDate desiredDate;
    private RequestStatus status;
    private Instant createdAt;
    private String clientEmail; // small denormalized data for admin/artisan lists

    public ServiceRequestResponse() {}

    public ServiceRequestResponse(Long id, Long categoryId, String title, String city, String description,
                                  LocalDate desiredDate, RequestStatus status, Instant createdAt, String clientEmail) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.city = city;
        this.description = description;
        this.desiredDate = desiredDate;
        this.status = status;
        this.createdAt = createdAt;
        this.clientEmail = clientEmail;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
}
