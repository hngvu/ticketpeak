package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.SeatRow;

public record SeatRowResponse(String id, String sectionId, String name) {
    public static SeatRowResponse from(SeatRow r) {
        return new SeatRowResponse(r.getId(), r.getSection().getId(), r.getName());
    }
}
