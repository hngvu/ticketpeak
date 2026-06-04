package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.SeatStatus;
import jakarta.validation.constraints.NotBlank;

public record CreateSeatRequest(
        @NotBlank String id,
        @NotBlank String name,
        Integer positionX,
        Integer positionY,
        Boolean accessibility,

        Boolean obstructedView,
        Boolean aisle,
        SeatStatus status
) {}
