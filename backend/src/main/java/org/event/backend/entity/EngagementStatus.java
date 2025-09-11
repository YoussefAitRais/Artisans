package org.event.backend.entity;

public enum EngagementStatus {
    PENDING_CONFIRMATION, // created after quote accepted; artisan must confirm schedule
    SCHEDULED,            // schedule confirmed (start/end set)
    IN_PROGRESS,          // work started
    COMPLETED,            // work finished
    CANCELLED             // cancelled before start (or by policy)
}
