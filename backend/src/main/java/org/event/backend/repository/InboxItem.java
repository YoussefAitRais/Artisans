// src/main/java/org/event/backend/repository/projections/InboxItem.java
package org.event.backend.repository;

import java.time.Instant;
import java.time.LocalDate;

public interface InboxItem {
    Long id();
    String title();
    String city();
    String description();
    LocalDate desiredDate();
    Instant createdAt();
    String status();      // PENDING | RESPONDED
    Long categoryId();
    String clientEmail();
}
