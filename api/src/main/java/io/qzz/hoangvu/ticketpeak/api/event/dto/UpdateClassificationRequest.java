package io.qzz.hoangvu.ticketpeak.api.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateClassificationRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "isActive is required")
    Boolean isActive,

    UUID parentId,

    Integer level
) {}
