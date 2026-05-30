package io.qzz.hoangvu.ticketpeak.api.eventgroup.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddEventToGroupRequest(
    @NotNull(message = "Event ID is required")
    UUID eventId,

    Integer displayOrder
) {}
