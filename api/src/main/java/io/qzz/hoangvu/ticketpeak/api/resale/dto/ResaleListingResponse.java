package io.qzz.hoangvu.ticketpeak.api.resale.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListing;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ResaleListingResponse(
        UUID id,
        UUID ticketId,
        UUID sellerId,
        UUID eventId,
        UUID offerId,
        UUID ticketTypeId,
        String ticketTypeName,
        String seatId,
        String sectionId,
        SeatingMode seatingMode,
        BigDecimal faceValue,
        BigDecimal askingPrice,
        String currency,
        ResaleListingStatus status,
        Instant reservedUntil,
        Instant createdAt,
        Instant updatedAt
) {
    public static ResaleListingResponse from(ResaleListing listing, boolean includeSellerId) {
        return new ResaleListingResponse(
                listing.getId(),
                listing.getTicketId(),
                includeSellerId ? listing.getSellerId() : null,
                listing.getEventId(),
                listing.getOfferId(),
                listing.getTicketTypeId(),
                listing.getTicketTypeName(),
                listing.getSeatId(),
                listing.getSectionId(),
                listing.getSeatingMode(),
                listing.getFaceValue(),
                listing.getAskingPrice(),
                listing.getCurrency(),
                listing.getStatus(),
                listing.getReservedUntil(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}
