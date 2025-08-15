package org.event.backend.entity;

/** Moderation/visibility lifecycle of a media item. */
public enum MediaStatus {
    PENDING,     // waiting for admin review (if moderation is enabled)
    APPROVED,    // visible to public
    REJECTED     // hidden / not approved
}
