package io.qzz.hoangvu.ticketpeak.api.reservation.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferCharge;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReservationItemResponse(
        UUID id,
        UUID offerId,
        SeatingMode seatingMode,
        String sectionId,
        String seatId,
        int qty,
        BigDecimal unitFaceValue,
        String currency,
        List<OfferCharge> charges
) {
    public static ReservationItemResponse from(ReservationItem item) {
        return new ReservationItemResponse(
                item.getId(),
                item.getOfferId(),
                item.getSeatingMode(),
                item.getSectionId(),
                item.getSeatId(),
                item.getQty(),
                item.getUnitFaceValue(),
                item.getCurrency(),
                item.getCharges()
        );
    }
}
