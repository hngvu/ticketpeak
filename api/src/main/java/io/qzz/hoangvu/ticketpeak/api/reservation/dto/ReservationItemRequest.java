package io.qzz.hoangvu.ticketpeak.api.reservation.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReservationItemRequest(
        @NotNull UUID offerId,
        @NotNull SeatingMode seatingMode,
        String sectionId,
        String seatId,
        @Min(1) Integer qty
) {}
