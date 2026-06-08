package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.GAArea;

import java.util.List;

public record GAAreaResponse(
        String id,
        String manifestId,
        String levelId,
        String priceLevelId,
        Integer capacity,
        Integer x,
        Integer y,
        Integer width,
        Integer height,
        List<List<Double>> polygon) {
    public static GAAreaResponse from(GAArea a) {
        return new GAAreaResponse(
                a.getId(),
                a.getManifestId(),
                a.getLevelId(),
                a.getPriceLevelId(),
                a.getCapacity(),
                a.getX(),
                a.getY(),
                a.getWidth(),
                a.getHeight(),
                a.getPolygon());
    }
}
