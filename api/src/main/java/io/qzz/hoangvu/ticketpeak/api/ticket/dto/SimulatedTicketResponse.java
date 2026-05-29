package io.qzz.hoangvu.ticketpeak.api.ticket.dto;

import java.util.UUID;

public record SimulatedTicketResponse(
        UUID ticketId,
        String offerName,
        String seatId,
        String status,
        String currentOtp,
        String qrPayload
) {}
