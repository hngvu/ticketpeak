package io.qzz.hoangvu.ticketpeak.api.resale.dto;

import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleOrder;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ResaleOrderResponse(
        UUID id,
        UUID resaleListingId,
        UUID buyerId,
        UUID paymentId,
        BigDecimal askingPrice,
        BigDecimal platformFeePercent,
        BigDecimal platformFeeAmount,
        BigDecimal netAmount,
        String currency,
        ResaleOrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ResaleOrderResponse from(ResaleOrder order) {
        return new ResaleOrderResponse(
                order.getId(),
                order.getResaleListingId(),
                order.getBuyerId(),
                order.getPaymentId(),
                order.getAskingPrice(),
                order.getPlatformFeePercent(),
                order.getPlatformFeeAmount(),
                order.getNetAmount(),
                order.getCurrency(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
