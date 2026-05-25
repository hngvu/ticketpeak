package io.qzz.hoangvu.ticketpeak.api.order.dto;

import io.qzz.hoangvu.ticketpeak.api.order.model.Order;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID reservationId,
        UUID paymentId,
        UUID accountId,
        UUID eventId,
        OrderStatus status,
        String currency,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems() != null
                ? order.getItems().stream().map(OrderItemResponse::from).toList()
                : List.of();

        return new OrderResponse(
                order.getId(),
                order.getReservationId(),
                order.getPaymentId(),
                order.getAccountId(),
                order.getEventId(),
                order.getStatus(),
                order.getCurrency(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
