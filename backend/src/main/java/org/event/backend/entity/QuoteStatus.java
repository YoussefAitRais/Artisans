package org.event.backend.entity;

/**
 * Lifecycle of a quote sent by an artisan for a service request.
 */
public enum QuoteStatus {
    SENT,       // initial
    ACCEPTED,   // accepted by client (only one per request)
    REJECTED    // rejected (automatically when another quote is accepted)
}
