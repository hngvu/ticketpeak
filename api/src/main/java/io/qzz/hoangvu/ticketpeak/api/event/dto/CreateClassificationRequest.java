package io.qzz.hoangvu.ticketpeak.api.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateClassificationRequest(
    @NotBlank(message = "Name is required")
    String name,

    String slug,

    @NotNull(message = "Level is required")
    Integer level,

    @NotNull(message = "isActive is required")
    Boolean isActive,

    UUID parentId
) {}
