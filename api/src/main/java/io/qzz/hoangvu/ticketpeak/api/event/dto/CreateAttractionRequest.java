package io.qzz.hoangvu.ticketpeak.api.event.dto;

import io.qzz.hoangvu.ticketpeak.api.event.model.AttractionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAttractionRequest(
    @NotBlank(message = "Name is required")
    String name,

    String slug,

    @NotNull(message = "Type is required")
    AttractionType type,

    String imageUrl,
    String description
) {}
