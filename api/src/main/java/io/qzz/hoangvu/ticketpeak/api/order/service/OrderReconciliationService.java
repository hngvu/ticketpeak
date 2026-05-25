package io.qzz.hoangvu.ticketpeak.api.order.service;

import io.qzz.hoangvu.ticketpeak.api.order.model.Order;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderStatus;
import io.qzz.hoangvu.ticketpeak.api.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
public class OrderReconciliationService {

    private final OrderRepository orderRepository;

    public OrderReconciliationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createFailedOrderInNewTx(
            UUID reservationId,
            UUID paymentId,
            UUID accountId,
            UUID eventId,
            BigDecimal total,
            String currency,
            String failureReason
    ) {
        log.info("Persisting FAILED order record for reservation {} in new transaction. Reason: {}",
                reservationId, failureReason);

        Order failedOrder = Order.builder()
                .reservationId(reservationId)
                .paymentId(paymentId)
                .accountId(accountId)
                .eventId(eventId)
                .status(OrderStatus.FAILED)
                .totalAmount(total)
                .currency(currency)
                .items(java.util.List.of()) // No items snapshot since it failed
                .build();

        orderRepository.saveAndFlush(failedOrder);
    }
}
