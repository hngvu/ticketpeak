package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public record UpdateManifestRequest(
        @NotBlank String description,
        @NotNull Integer totalCapacity,
        List<Map<String, Object>> objects
) {}
