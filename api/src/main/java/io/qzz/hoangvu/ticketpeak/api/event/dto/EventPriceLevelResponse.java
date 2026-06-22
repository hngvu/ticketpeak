package io.qzz.hoangvu.ticketpeak.api.event.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.dto.PriceLevelResponse;

import java.math.BigDecimal;

public record EventPriceLevelResponse(
        String id,
        String name,
        String description,
        String color,
        BigDecimal price
) {
    public static EventPriceLevelResponse from(PriceLevelResponse pl, String offerName, BigDecimal price) {
        return new EventPriceLevelResponse(pl.id(), offerName != null ? offerName : pl.description(), pl.description(), pl.color(), price);
    }
}
