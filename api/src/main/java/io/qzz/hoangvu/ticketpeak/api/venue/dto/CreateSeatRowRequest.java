package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSeatRowRequest(
        @NotBlank String id,
        @NotBlank String name,
        Integer positionY
) {}
