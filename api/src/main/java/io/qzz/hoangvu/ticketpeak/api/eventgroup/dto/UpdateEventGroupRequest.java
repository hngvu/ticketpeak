package io.qzz.hoangvu.ticketpeak.api.eventgroup.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record UpdateEventGroupRequest(
    @NotBlank(message = "Name is required")
    String name,

    String slug,
    String description,
    String imageUrl,
    Boolean isActive,
    List<UUID> eventIds
) {}
