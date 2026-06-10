package io.qzz.hoangvu.ticketpeak.api.order.event;

import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemSnapshot(
        UUID id,
        UUID offerId,
        UUID ticketTypeId,
        String ticketTypeName,
        String offerName,
        BigDecimal faceValue,
        String currency,
        int qty,
        SeatingMode seatingMode,
        String sectionId,
        String seatId
) {}
