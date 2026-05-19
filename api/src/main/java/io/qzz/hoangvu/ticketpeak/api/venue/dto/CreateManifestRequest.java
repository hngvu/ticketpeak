package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateManifestRequest(
        @NotBlank String id,
        @NotBlank String description,
        @NotNull Integer totalCapacity
) {}
