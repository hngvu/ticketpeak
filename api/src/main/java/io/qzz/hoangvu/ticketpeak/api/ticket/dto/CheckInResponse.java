package io.qzz.hoangvu.ticketpeak.api.ticket.dto;

import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;

import java.time.Instant;
import java.util.UUID;

public record CheckInResponse(
        boolean success,
        boolean alreadyCheckedIn,
        UUID ticketId,
        String seatId,
        String areaId,
        String seatingMode,
        Instant checkedInAt
) {
    public static CheckInResponse success(Ticket t) {
        return new CheckInResponse(
                true,
                false,
                t.getId(),
                t.getSeatId(),
                t.getAreaId(),
                t.getSeatingMode().name(),
                t.getCheckedInAt()
        );
    }

    public static CheckInResponse alreadyCheckedIn(Ticket t) {
        return new CheckInResponse(
                true,
                true,
                t.getId(),
                t.getSeatId(),
                t.getAreaId(),
                t.getSeatingMode().name(),
                t.getCheckedInAt()
        );
    }
}
