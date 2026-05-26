package io.qzz.hoangvu.ticketpeak.api.ticket.dto;

import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketTransfer;

import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        UUID ticketId,
        UUID senderId,
        UUID recipientId,
        String status,
        Instant expiresAt
) {
    public static TransferResponse from(TicketTransfer t) {
        return new TransferResponse(
                t.getId(),
                t.getTicketId(),
                t.getSenderId(),
                t.getRecipientId(),
                t.getStatus().name(),
                t.getExpiresAt()
        );
    }
}
