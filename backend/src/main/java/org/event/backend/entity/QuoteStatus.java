package org.event.backend.entity;


public enum QuoteStatus {
    SENT,       // initial
    ACCEPTED,   // accepted by client (only one per request)
    REJECTED    // rejected (automatically when another quote is accepted)
}
