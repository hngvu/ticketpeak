package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferSaleWindow;
import java.time.Instant;
import java.util.UUID;

public record OfferSaleWindowResponse(
        UUID id,
        String type,
        Instant startAt,
        Instant endAt
) {
    public static OfferSaleWindowResponse from(OfferSaleWindow window) {
        return new OfferSaleWindowResponse(
                window.getId(),
                window.getType(),
                window.getStartAt(),
                window.getEndAt()
        );
    }
}
