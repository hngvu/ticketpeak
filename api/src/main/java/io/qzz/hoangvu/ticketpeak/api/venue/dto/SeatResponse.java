package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Seat;
import io.qzz.hoangvu.ticketpeak.api.venue.model.SeatStatus;

public record SeatResponse(String id, String rowId, String name, Integer positionX, Integer positionY, SeatStatus status, Boolean accessibility, Boolean obstructedView, Boolean aisle, String priceLevelId, String sectionId) {
    public static SeatResponse from(Seat s) {
        return new SeatResponse(s.getId(), s.getSeatRow().getId(), s.getName(), s.getPositionX(), s.getPositionY(), s.getStatus(), s.getAccessibility(), s.getObstructedView(), s.getAisle(), s.getPriceLevelId(), s.getSectionId());
    }

}
