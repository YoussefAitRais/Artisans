package org.event.backend.entity;

/**
 * Lifecycle for a service request.
 */
public enum RequestStatus {
    PENDING,     // created by client, editable/cancellable
    RESPONDED,   // at least one artisan responded (future use with quotes)
    ACCEPTED,    // client accepted an offer (future)
    REJECTED,    // client rejected / no longer pursuing
    CANCELLED,   // client cancelled while pending
    COMPLETED    // job done (future)
}
