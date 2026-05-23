package io.qzz.hoangvu.ticketpeak.api.inventory.dto;

import java.util.List;
import java.util.UUID;

public record EventInventoryStatusResponse(
    UUID eventId,
    List<GAInventoryStatus> gaInventory,
    List<ReservedSeatStatus> reservedSeats
) {
    public record GAInventoryStatus(
        String areaId,
        int capacity,
        int sold,
        int available
    ) {}

    public record ReservedSeatStatus(
        String seatId,
        String status // "AVAILABLE", "SOLD"
    ) {}
}
