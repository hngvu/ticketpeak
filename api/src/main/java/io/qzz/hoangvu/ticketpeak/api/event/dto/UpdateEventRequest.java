package io.qzz.hoangvu.ticketpeak.api.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UpdateEventRequest(
    @NotNull(message = "Venue ID is required")
    UUID venueId,

    @NotBlank(message = "Title is required")
    String title,

    String description,

    @NotNull(message = "Start time is required")
    Instant startAt,

    Instant endAt,

    @NotBlank(message = "Timezone is required")
    String timezone,

    Instant saleStartAt,
    Instant saleEndAt,
    boolean restrictSingleSeat,
    boolean safeTixEnabled,
    boolean transferEnabled,
    List<UUID> attractionIds,
    List<UUID> classificationIds
) {}
