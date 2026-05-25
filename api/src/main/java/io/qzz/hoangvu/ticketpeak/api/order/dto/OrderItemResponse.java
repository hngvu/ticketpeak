package io.qzz.hoangvu.ticketpeak.api.order.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public record OrderItemResponse(
        UUID id,
        UUID offerId,
        SeatingMode seatingMode,
        String areaId,
        String seatId,
        int qty,
        BigDecimal unitFaceValue,
        BigDecimal unitTotalPrice,
        BigDecimal lineTotal,
        String currency,
        List<OrderChargeResponse> charges
) {

    public static OrderItemResponse from(OrderItem item) {
        List<OrderChargeResponse> mappedCharges = item.getCharges().stream()
                .map(c -> new OrderChargeResponse(c.name(), c.type().name(), c.amount(), c.isPercentage()))
                .toList();

        return new OrderItemResponse(
                item.getId(),
                item.getOfferId(),
                item.getSeatingMode(),
                item.getAreaId(),
                item.getSeatId(),
                item.getQty(),
                item.getUnitFaceValue(),
                item.getUnitTotalPrice(),
                item.getLineTotal(),
                item.getCurrency(),
                mappedCharges
        );
    }
}
