package io.qzz.hoangvu.ticketpeak.api.eventgroup.dto;

import io.qzz.hoangvu.ticketpeak.api.event.dto.EventResponse;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.model.EventGroup;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventGroupResponse(
    UUID id,
    UUID organizationId,
    String name,
    String slug,
    String description,
    String imageUrl,
    Boolean isActive,
    List<EventResponse> events,
    Instant createdAt,
    Instant updatedAt
) {
    public static EventGroupResponse from(EventGroup eg, List<EventResponse> events) {
        return new EventGroupResponse(
            eg.getId(),
            eg.getOrganizationId(),
            eg.getName(),
            eg.getSlug(),
            eg.getDescription(),
            eg.getImageUrl(),
            eg.getIsActive(),
            events,
            eg.getCreatedAt(),
            eg.getUpdatedAt()
        );
    }
}
