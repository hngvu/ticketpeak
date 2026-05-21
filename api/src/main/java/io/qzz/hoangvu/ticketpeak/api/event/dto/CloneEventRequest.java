package io.qzz.hoangvu.ticketpeak.api.event.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record CloneEventRequest(
    @NotBlank(message = "New title is required")
    String title,

    String slug,
    Instant startAt,
    Instant endAt,
    Instant saleStartAt,
    Instant saleEndAt
) {}
