package io.qzz.hoangvu.ticketpeak.api.eventgroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateEventGroupRequest(
    @NotNull(message = "Organization ID is required")
    UUID organizationId,

    @NotBlank(message = "Name is required")
    String name,

    String slug,
    String description,
    String imageUrl,
    List<UUID> eventIds
) {}
