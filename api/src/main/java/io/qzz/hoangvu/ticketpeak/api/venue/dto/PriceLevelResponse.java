package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.PriceLevel;

public record PriceLevelResponse(String id, String manifestId, String description, String color) {
    public static PriceLevelResponse from(PriceLevel p) {
        return new PriceLevelResponse(p.getId(), p.getManifest().getId(), p.getDescription(), p.getColor());
    }
}
