package io.qzz.hoangvu.ticketpeak.api.order.service;

import io.qzz.hoangvu.ticketpeak.api.inventory.service.InventoryService;
import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferCharge;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.order.dto.OrderResponse;
import io.qzz.hoangvu.ticketpeak.api.order.exception.OrderException;
import io.qzz.hoangvu.ticketpeak.api.order.model.Order;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderItem;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderStatus;
import io.qzz.hoangvu.ticketpeak.api.order.repository.OrderRepository;
import io.qzz.hoangvu.ticketpeak.api.payment.event.PaymentCompletedEvent;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationItem;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import io.qzz.hoangvu.ticketpeak.api.reservation.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.context.ApplicationEventPublisher;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.TicketTypeRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.TicketType;
import io.qzz.hoangvu.ticketpeak.api.order.event.OrderCreatedEvent;
import io.qzz.hoangvu.ticketpeak.api.order.event.OrderItemSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final InventoryService inventoryService;
    private final OrderReconciliationService reconciliationService;
    private final ApplicationEventPublisher eventPublisher;
    private final OfferRepository offerRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public OrderService(
            OrderRepository orderRepository,
            ReservationRepository reservationRepository,
            InventoryService inventoryService,
            OrderReconciliationService reconciliationService,
            ApplicationEventPublisher eventPublisher,
            OfferRepository offerRepository,
            TicketTypeRepository ticketTypeRepository
    ) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
        this.reconciliationService = reconciliationService;
        this.eventPublisher = eventPublisher;
        this.offerRepository = offerRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    // ─── Event Listener: Order Creation ──────────────────────────────────

    /**
     * Listens for PaymentCompletedEvent AFTER the payment transaction commits.
     * Creates an order from the confirmed reservation, confirms inventory holds,
     * and transitions the reservation to FINALIZED.
     *
     * <p>Safety sequence:
     * <ol>
     *   <li>Idempotency guard — skip if order already exists for this reservation</li>
     *   <li>Fetch reservation with pessimistic write lock</li>
     *   <li>Confirm inventory holds (GA → sold, seat → sold) — if this fails, the whole TX rolls back</li>
     *   <li>Build and save Order entity with snapshot items</li>
     *   <li>Transition reservation status to FINALIZED</li>
     * </ol>
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        UUID reservationId = event.getReservationId();
        UUID paymentId = event.getPaymentId();

        log.info("Processing PaymentCompletedEvent: reservation={}, payment={}", reservationId, paymentId);

        // 1. Idempotency guard
        if (orderRepository.existsByReservationIdAndStatus(reservationId, OrderStatus.CONFIRMED)) {
            log.info("Order already created for reservation {}. Skipping duplicate processing.", reservationId);
            return;
        }

        try {
            // 2. Fetch reservation with pessimistic write lock
            Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                    .orElseThrow(() -> {
                        log.error("Reservation {} not found during order creation", reservationId);
                        return OrderException.inventoryConfirmFailed(
                                "Reservation " + reservationId + " not found");
                    });

            // Double-check idempotency after acquiring lock
            if (reservation.getStatus() == ReservationStatus.FINALIZED) {
                log.info("Reservation {} already FINALIZED. Skipping.", reservationId);
                return;
            }

            if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                log.warn("Reservation {} is in unexpected status {} during order creation. Skipping.",
                        reservationId, reservation.getStatus());
                return;
            }

            // 3. Confirm inventory holds FIRST
            confirmInventoryHolds(reservation);

            // 4. Build and save Order entity with snapshot items
            Order order = buildOrder(reservation, paymentId, event.getAmount(), event.getCurrency());
            orderRepository.save(order);

            // 5. Transition reservation to FINALIZED
            reservation.setStatus(ReservationStatus.FINALIZED);
            reservationRepository.save(reservation);

            // 6. Publish OrderCreatedEvent
            List<OrderItemSnapshot> snapshots = buildSnapshots(order);
            eventPublisher.publishEvent(new OrderCreatedEvent(
                    this,
                    order.getId(),
                    order.getAccountId(),
                    order.getEventId(),
                    snapshots
            ));

            log.info("Order created successfully for reservation {}. Order ID: {}",
                    reservationId, order.getId());

        } catch (Exception e) {
            log.error("Failed to create order for reservation {}. Recording audit trail.", reservationId, e);

            // Record a FAILED order in a separate TX for audit trailing
            try {
                reconciliationService.createFailedOrderInNewTx(
                        reservationId,
                        paymentId,
                        event.getAccountId(),
                        event.getEventId(),
                        event.getAmount(),
                        event.getCurrency(),
                        e.getMessage()
                );
            } catch (Exception auditEx) {
                log.error("Failed to record audit trail for reservation {}", reservationId, auditEx);
            }

            throw e; // Rethrow to rollback the REQUIRES_NEW transaction
        }
    }

    // ─── Read Endpoints ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID accountId, UUID orderId) {
        Order order = orderRepository.findByIdAndAccountId(orderId, accountId)
                .orElseThrow(OrderException::notFound);

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(UUID accountId, Pageable pageable) {
        return orderRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(OrderResponse::from);
    }

    // ─── Private Helpers ────────────────────────────────────────────────

    private void confirmInventoryHolds(Reservation reservation) {
        for (ReservationItem item : reservation.getItems()) {
            if (item.getSeatingMode() == SeatingMode.GENERAL_ADMISSION) {
                inventoryService.confirmGAInventory(
                        reservation.getEventId(),
                        item.getSectionId(),
                        item.getOfferId(),
                        item.getQty()
                );
            } else if (item.getSeatingMode() == SeatingMode.RESERVED_SEATING) {
                inventoryService.sellSeat(
                        reservation.getEventId(),
                        item.getSeatId()
                );
            }
        }
    }

    private Order buildOrder(Reservation reservation, UUID paymentId, BigDecimal totalAmount, String currency) {
        Order order = Order.builder()
                .reservationId(reservation.getId())
                .paymentId(paymentId)
                .accountId(reservation.getAccountId())
                .eventId(reservation.getEventId())
                .status(OrderStatus.CONFIRMED)
                .totalAmount(totalAmount)
                .currency(currency)
                .items(new ArrayList<>())
                .build();

        for (ReservationItem resItem : reservation.getItems()) {
            BigDecimal unitTotalPrice = calculateUnitTotalPrice(resItem);
            BigDecimal lineTotal = unitTotalPrice.multiply(BigDecimal.valueOf(resItem.getQty()))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .offerId(resItem.getOfferId())
                    .seatingMode(resItem.getSeatingMode())
                    .sectionId(resItem.getSectionId())
                    .seatId(resItem.getSeatId())
                    .qty(resItem.getQty())
                    .unitFaceValue(resItem.getUnitFaceValue())
                    .unitTotalPrice(unitTotalPrice)
                    .lineTotal(lineTotal)
                    .currency(resItem.getCurrency())
                    .charges(resItem.getCharges() != null ? resItem.getCharges() : List.of())
                    .build();

            order.getItems().add(orderItem);
        }

        BigDecimal sumOfLines = order.getItems().stream()
                .map(OrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumOfLines.compareTo(totalAmount) != 0) {
            log.warn("Order totalAmount {} differs from sum of lineTotals {} for reservation {}",
                    totalAmount, sumOfLines, reservation.getId());
        }

        return order;
    }

    private BigDecimal calculateUnitTotalPrice(ReservationItem item) {
        BigDecimal unitFaceValue = item.getUnitFaceValue();
        BigDecimal totalCharges = BigDecimal.ZERO;

        if (item.getCharges() != null) {
            for (OfferCharge charge : item.getCharges()) {
                BigDecimal chargeAmount;
                if (charge.isPercentage()) {
                    chargeAmount = unitFaceValue.multiply(charge.amount())
                            .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                } else {
                    chargeAmount = charge.amount();
                }
                totalCharges = totalCharges.add(chargeAmount);
            }
        }

        return unitFaceValue.add(totalCharges).setScale(2, RoundingMode.HALF_UP);
    }

    private List<OrderItemSnapshot> buildSnapshots(Order order) {
        List<UUID> offerIds = order.getItems().stream()
                .map(OrderItem::getOfferId)
                .distinct()
                .toList();

        List<Offer> offers = offerRepository.findAllById(offerIds);
        java.util.Map<UUID, Offer> offerMap = new java.util.HashMap<>();
        for (Offer o : offers) {
            offerMap.put(o.getId(), o);
        }

        List<UUID> ticketTypeIds = offers.stream()
                .map(Offer::getTicketTypeId)
                .distinct()
                .toList();

        List<TicketType> ticketTypes = ticketTypeRepository.findAllById(ticketTypeIds);
        java.util.Map<UUID, TicketType> ticketTypeMap = new java.util.HashMap<>();
        for (TicketType t : ticketTypes) {
            ticketTypeMap.put(t.getId(), t);
        }

        List<OrderItemSnapshot> snapshots = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Offer offer = offerMap.get(item.getOfferId());
            if (offer == null) {
                throw OrderException.inventoryConfirmFailed("Offer not found: " + item.getOfferId());
            }
            TicketType ticketType = ticketTypeMap.get(offer.getTicketTypeId());
            if (ticketType == null) {
                throw OrderException.inventoryConfirmFailed("TicketType not found: " + offer.getTicketTypeId());
            }
            
            snapshots.add(new OrderItemSnapshot(
                    item.getId(),
                    item.getOfferId(),
                    offer.getTicketTypeId(),
                    ticketType.getName(),
                    offer.getName(),
                    item.getUnitFaceValue(),
                    item.getCurrency(),
                    item.getQty(),
                    item.getSeatingMode(),
                    item.getSectionId(),
                    item.getSeatId()
            ));
        }
        return snapshots;
    }
}
