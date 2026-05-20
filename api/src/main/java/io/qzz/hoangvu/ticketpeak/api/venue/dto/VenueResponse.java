package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Venue;
import io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VenueResponse(
        UUID id,
        String name,
        String address,
        String city,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String email,
        String website,
        String description,
        String thumbnailUrl,
        List<String> images,
        VenueStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static VenueResponse from(Venue v) {
        return new VenueResponse(v.getId(), v.getName(), v.getAddress(), v.getCity(), v.getCountry(),
                v.getLatitude(), v.getLongitude(), v.getPhone(), v.getEmail(), v.getWebsite(),
                v.getDescription(), v.getThumbnailUrl(), v.getImages(), v.getStatus(),
                v.getCreatedAt(), v.getUpdatedAt());
    }
}
