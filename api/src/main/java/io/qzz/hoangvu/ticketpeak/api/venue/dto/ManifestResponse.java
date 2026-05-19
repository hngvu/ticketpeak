package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Manifest;
import io.qzz.hoangvu.ticketpeak.api.venue.model.ManifestStatus;

import java.time.Instant;

public record ManifestResponse(
        String id,
        String venueId,
        String description,
        Integer totalCapacity,
        ManifestStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ManifestResponse from(Manifest m) {
        return new ManifestResponse(m.getId(), m.getVenue().getId(), m.getDescription(),
                m.getTotalCapacity(), m.getStatus(), m.getCreatedAt(), m.getUpdatedAt());
    }
}
