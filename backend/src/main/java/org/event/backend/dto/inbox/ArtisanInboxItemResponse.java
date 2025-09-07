// src/main/java/org/event/backend/dto/inbox/ArtisanInboxItemResponse.java
package org.event.backend.dto.inbox;

import java.time.Instant;
import java.time.LocalDate;

public class ArtisanInboxItemResponse {

    public Long id;
    public String title;
    public String city;
    public String description;
    public LocalDate desiredDate;
    public Instant createdAt;
    public String status; // PENDING | RESPONDED
    public Long categoryId;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getDesiredDate() {
        return desiredDate;
    }

    public void setDesiredDate(LocalDate desiredDate) {
        this.desiredDate = desiredDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public ArtisanInboxItemResponse() {}
    public ArtisanInboxItemResponse(Long id, String title, String city, String description,
                                    LocalDate desiredDate, Instant createdAt,
                                    String status, Long categoryId) {
        this.id = id; this.title = title; this.city = city; this.description = description;
        this.desiredDate = desiredDate; this.createdAt = createdAt; this.status = status;
        this.categoryId = categoryId;
    }
}
