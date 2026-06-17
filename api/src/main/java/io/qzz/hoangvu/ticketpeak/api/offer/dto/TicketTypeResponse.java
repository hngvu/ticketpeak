package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.TicketType;

import java.time.Instant;
import java.util.UUID;

public record TicketTypeResponse(
        UUID id,
        UUID eventId,
        String name,
        String code,
        Instant createdAt,
        Instant updatedAt
) {
    public static TicketTypeResponse from(TicketType t) {
        return new TicketTypeResponse(
                t.getId(),
                t.getEventId(),
                t.getName(),
                t.getCode(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
