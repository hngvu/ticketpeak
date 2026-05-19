package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Seat;
import io.qzz.hoangvu.ticketpeak.api.venue.model.SeatStatus;

public record SeatResponse(String id, String rowId, String name, Integer positionX, SeatStatus status, Boolean accessibility, Boolean obstructedView, Boolean aisle) {
    public static SeatResponse from(Seat s) {
        return new SeatResponse(s.getId(), s.getSeatRow().getId(), s.getName(), s.getPositionX(), s.getStatus(), s.getAccessibility(), s.getObstructedView(), s.getAisle());
    }
}
