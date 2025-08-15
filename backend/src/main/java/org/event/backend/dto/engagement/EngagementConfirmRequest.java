package org.event.backend.dto.engagement;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Artisan sets/confirm the schedule. */
public class EngagementConfirmRequest {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
