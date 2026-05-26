package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OfferResponse(
        UUID id,
        UUID eventId,
        UUID ticketTypeId,
        String name,
        String description,
        String currency,
        BigDecimal faceValue,
        boolean restrictedPayment,
        Integer eventTicketMinimum,
        Integer capacityCap,
        List<Integer> sellableQuantities,
        List<OfferSaleWindowResponse> saleWindows,
        SeatingMode seatingMode,
        String sectionId,
        String priceLevelId,
        List<OfferChargeResponse> charges,
        Instant createdAt,
        Instant updatedAt
) {
    public static OfferResponse from(Offer offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getEventId(),
                offer.getTicketTypeId(),
                offer.getName(),
                offer.getDescription(),
                offer.getCurrency(),
                offer.getFaceValue(),
                offer.isRestrictedPayment(),
                offer.getEventTicketMinimum(),
                offer.getCapacityCap(),
                offer.getSellableQuantities(),
                offer.getSaleWindows() == null ? List.of() : offer.getSaleWindows().stream().map(OfferSaleWindowResponse::from).toList(),
                offer.getSeatingMode(),
                offer.getSectionId(),
                offer.getPriceLevelId(),
                offer.getCharges() == null ? List.of() : offer.getCharges().stream().map(OfferChargeResponse::from).toList(),
                offer.getCreatedAt(),
                offer.getUpdatedAt()
        );
    }
}
