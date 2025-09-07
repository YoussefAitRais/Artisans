package org.event.backend.dto.inbox;

import java.time.Instant;
import java.time.LocalDate;


public record InboxItem(
        Long id,
        String title,
        String city,
        String description,
        LocalDate desiredDate,
        Instant createdAt,
        String status,
        Long categoryId,
        String clientEmail
) {}
