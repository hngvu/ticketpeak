package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRSAreaRequest(
        @NotBlank String id,
        @NotBlank String levelId,
        @NotBlank String sectionId,
        @NotBlank String priceLevelId
) {}
