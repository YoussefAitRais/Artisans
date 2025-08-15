package org.event.backend.entity;

/** Lifecycle of an accepted job between a client and an artisan. */
public enum EngagementStatus {
    PENDING_CONFIRMATION, // created after quote accepted; artisan must confirm schedule
    SCHEDULED,            // schedule confirmed (start/end set)
    IN_PROGRESS,          // work started
    COMPLETED,            // work finished
    CANCELLED             // cancelled before start (or by policy)
}
