package io.qzz.hoangvu.ticketpeak.api.inventory.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HoldInventoryResponse(
    String holdToken,
    Instant expiresAt,
    UUID eventId,
    List<String> heldSeatIds,
    List<GAHoldDetail> heldGAHolds
) {
    public record GAHoldDetail(
        String areaId,
        int quantity
    ) {}
}
