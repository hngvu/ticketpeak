package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.SeatRow;

public record SeatRowResponse(String id, String rsAreaId, String name, Integer positionY) {
    public static SeatRowResponse from(SeatRow r) {
        return new SeatRowResponse(r.getId(), r.getRsArea().getId(), r.getName(), r.getPositionY());
    }
}
