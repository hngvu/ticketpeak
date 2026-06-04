package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.RSArea;

public record RSAreaResponse(
        String id,
        String manifestId,
        String levelId,
        String sectionId,
        String priceLevelId,
        Integer x,
        Integer y,
        Integer width,
        Integer height) {
    public static RSAreaResponse from(RSArea a) {
        return new RSAreaResponse(
                a.getId(),
                a.getManifestId(),
                a.getLevelId(),
                a.getSectionId(),
                a.getPriceLevelId(),
                a.getX(),
                a.getY(),
                a.getWidth(),
                a.getHeight());
    }
}
