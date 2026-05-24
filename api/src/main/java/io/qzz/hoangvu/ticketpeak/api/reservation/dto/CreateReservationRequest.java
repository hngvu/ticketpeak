package io.qzz.hoangvu.ticketpeak.api.reservation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateReservationRequest(
        @NotNull UUID eventId,
        @NotEmpty List<@Valid ReservationItemRequest> items
) {}
