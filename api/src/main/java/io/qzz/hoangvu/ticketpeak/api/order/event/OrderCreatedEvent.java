package io.qzz.hoangvu.ticketpeak.api.order.event;

import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        Object source,
        UUID orderId,
        UUID accountId,
        UUID eventId,
        List<OrderItemSnapshot> items
) {}
