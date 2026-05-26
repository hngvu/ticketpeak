package io.qzz.hoangvu.ticketpeak.api.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(
        @NotBlank String qrPayload
) {}
