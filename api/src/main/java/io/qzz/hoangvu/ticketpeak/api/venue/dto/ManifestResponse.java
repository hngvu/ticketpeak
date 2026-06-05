package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Manifest;
import io.qzz.hoangvu.ticketpeak.api.venue.model.ManifestStatus;

import java.time.Instant;
import java.util.UUID;

import java.util.UUID;

import java.util.List;
import java.util.Map;

public record ManifestResponse(
        String id,
        UUID venueId,
        String description,
        Integer totalCapacity,
        ManifestStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<Map<String, Object>> objects
) {
    public static ManifestResponse from(Manifest m) {
        return new ManifestResponse(m.getId(), m.getVenue().getId(), m.getDescription(),
                m.getTotalCapacity(), m.getStatus(), m.getCreatedAt(), m.getUpdatedAt(), m.getObjects());
    }
}
