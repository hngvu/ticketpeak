package io.qzz.hoangvu.ticketpeak.api.event.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record PostponeEventRequest(
    @NotNull(message = "New start time is required")
    Instant newStartAt,

    Instant newEndAt,
    Instant newSaleStartAt,
    Instant newSaleEndAt
) {}
