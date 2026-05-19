package io.qzz.hoangvu.ticketpeak.api.venue.model;

public enum ManifestStatus {
    DRAFT,      // Being configured, not yet usable
    PUBLISHED,  // Active, can be attached to events
    ARCHIVED    // No longer in use
}
