package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;

public record CreateRSAreaRequest(
        @NotBlank String id,
        @NotBlank String levelId,
        @NotBlank String sectionId,
        @NotBlank String priceLevelId,
        @PositiveOrZero Integer x,
        @PositiveOrZero Integer y,
        @Positive Integer width,
        @Positive Integer height
) {}
