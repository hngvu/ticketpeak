package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.GAArea;

public record GAAreaResponse(
        String id,
        String manifestId,
        String levelId,
        String sectionId,
        String priceLevelId,
        Integer capacity,
        Integer x,
        Integer y,
        Integer width,
        Integer height) {
    public static GAAreaResponse from(GAArea a) {
        return new GAAreaResponse(
                a.getId(),
                a.getManifestId(),
                a.getLevelId(),
                a.getSectionId(),
                a.getPriceLevelId(),
                a.getCapacity(),
                a.getX(),
                a.getY(),
                a.getWidth(),
                a.getHeight());
    }
}
