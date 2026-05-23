package io.qzz.hoangvu.ticketpeak.api.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record HoldInventoryRequest(
    @NotNull(message = "must not be null")
    UUID eventId,

    List<String> seatIds,

    @Valid
    List<GAHoldRequest> gaHolds,

    String holdToken
) {}
