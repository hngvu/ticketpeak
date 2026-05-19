package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSeatRequest(
        @NotBlank String id,
        @NotBlank String name,
        Integer positionX,
        Boolean accessibility,
        Boolean obstructedView,
        Boolean aisle
) {}
