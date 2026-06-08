package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateGAAreaRequest(
        @NotBlank String id,
        @NotBlank String levelId,
        String priceLevelId,
        @NotNull @Positive Integer capacity,
        @PositiveOrZero Integer x,
        @PositiveOrZero Integer y,
        @Positive Integer width,
        @Positive Integer height,
        List<List<Double>> polygon
) {}
