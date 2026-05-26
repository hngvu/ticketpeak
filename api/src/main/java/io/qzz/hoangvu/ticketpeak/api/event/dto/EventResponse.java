package io.qzz.hoangvu.ticketpeak.api.event.dto;

import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(
    UUID id,
    UUID organizationId,
    UUID venueId,
    String title,
    String slug,
    String description,
    EventStatus status,
    Instant startAt,
    Instant endAt,
    String timezone,
    Instant saleStartAt,
    Instant saleEndAt,
    boolean restrictSingleSeat,
    boolean safeTixEnabled,
    boolean transferEnabled,
    int maxTransferCount,
    BigDecimal serviceFeePercent,
    List<AttractionResponse> attractions,
    List<ClassificationResponse> classifications,
    String manifestId,
    Instant createdAt,
    Instant updatedAt
) {
    public static EventResponse from(Event e, List<AttractionResponse> attractions, List<ClassificationResponse> classifications, String manifestId) {
        return new EventResponse(
            e.getId(),
            e.getOrganizationId(),
            e.getVenueId(),
            e.getTitle(),
            e.getSlug(),
            e.getDescription(),
            e.getStatus(),
            e.getStartAt(),
            e.getEndAt(),
            e.getTimezone(),
            e.getSaleStartAt(),
            e.getSaleEndAt(),
            e.isRestrictSingleSeat(),
            e.isSafeTixEnabled(),
            e.isTransferEnabled(),
            e.getMaxTransferCount(),
            e.getServiceFeePercent(),
            attractions,
            classifications,
            manifestId,
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
