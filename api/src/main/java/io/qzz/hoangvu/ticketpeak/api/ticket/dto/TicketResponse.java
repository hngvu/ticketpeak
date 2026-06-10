package io.qzz.hoangvu.ticketpeak.api.ticket.dto;

import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID orderId,
        UUID eventId,
        UUID offerId,
        UUID ticketTypeId,
        String ticketTypeName,
        String offerName,
        BigDecimal faceValue,
        String currency,
        String seatingMode,
        String sectionId,
        String seatId,
        String status,
        Instant checkedInAt,
        int transferCount,
        Instant createdAt
) {
    public static TicketResponse from(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getOrderId(),
                t.getEventId(),
                t.getOfferId(),
                t.getTicketTypeId(),
                t.getTicketTypeName(),
                t.getOfferName(),
                t.getFaceValue(),
                t.getCurrency(),
                t.getSeatingMode().name(),
                t.getSectionId(),
                t.getSeatId(),
                t.getStatus().name(),
                t.getCheckedInAt(),
                t.getTransferCount(),
                t.getCreatedAt()
        );
    }
}
