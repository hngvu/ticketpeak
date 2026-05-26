package io.qzz.hoangvu.ticketpeak.api.event.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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

    @Min(value = 0, message = "Max transfer count must be at least 0")
    int maxTransferCount,

    @DecimalMin(value = "0.00", message = "Service fee percent must be at least 0")
    @DecimalMax(value = "100.00", message = "Service fee percent must not exceed 100")
    BigDecimal serviceFeePercent,

    List<UUID> attractionIds,
    List<UUID> classificationIds
) {}
