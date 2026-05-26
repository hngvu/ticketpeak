package io.qzz.hoangvu.ticketpeak.api.ticket.dto;

import java.util.UUID;

public record TicketQrResponse(
        UUID ticketId,
        String otp,
        long windowExpiresAt
) {}
