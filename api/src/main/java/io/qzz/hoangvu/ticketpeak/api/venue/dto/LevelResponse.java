package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Level;

public record LevelResponse(String id, String manifestId, String description, String color) {
    public static LevelResponse from(Level l) {
        return new LevelResponse(l.getId(), l.getManifest().getId(), l.getDescription(), l.getColor());
    }
}
