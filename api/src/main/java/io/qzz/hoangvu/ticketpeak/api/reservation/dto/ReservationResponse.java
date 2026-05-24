package io.qzz.hoangvu.ticketpeak.api.reservation.dto;

import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID eventId,
        ReservationStatus status,
        String currency,
        Instant expiresAt,
        List<ReservationItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getEventId(),
                r.getStatus(),
                r.getCurrency(),
                r.getExpiresAt(),
                r.getItems().stream().map(ReservationItemResponse::from).toList(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
