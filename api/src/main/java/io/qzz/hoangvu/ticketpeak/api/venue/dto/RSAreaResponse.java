package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.RSArea;

import java.util.List;

public record RSAreaResponse(
        String id,
        String manifestId,
        String levelId,
        Integer x,
        Integer y,
        Integer width,
        Integer height,
        List<List<Double>> polygon) {
    public static RSAreaResponse from(RSArea a) {
        return new RSAreaResponse(
                a.getId(),
                a.getManifestId(),
                a.getLevelId(),
                a.getX(),
                a.getY(),
                a.getWidth(),
                a.getHeight(),
                a.getPolygon());
    }
}
