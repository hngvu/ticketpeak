package io.qzz.hoangvu.ticketpeak.api.ticket.service;

import io.qzz.hoangvu.ticketpeak.api.order.event.OrderCreatedEvent;
import io.qzz.hoangvu.ticketpeak.api.order.event.OrderItemSnapshot;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketStatus;
import io.qzz.hoangvu.ticketpeak.api.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketIssuanceService {

    private final TicketRepository ticketRepository;
    private final TotpService totpService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderCreated(OrderCreatedEvent event) {
        UUID orderId = event.orderId();

        // Idempotency guard
        if (ticketRepository.existsByOrderId(orderId)) {
            log.info("Tickets already issued for order {}. Skipping.", orderId);
            return;
        }

        List<Ticket> tickets = new ArrayList<>();

        for (OrderItemSnapshot item : event.items()) {
            for (int i = 0; i < item.qty(); i++) {
                String rawSecret = totpService.generateSecret();
                String encryptedSecret = totpService.encrypt(rawSecret);

                tickets.add(Ticket.builder()
                        .orderId(orderId)
                        .orderItemId(item.id())
                        .accountId(event.accountId())
                        .eventId(event.eventId())
                        .offerId(item.offerId())
                        .ticketTypeId(item.ticketTypeId())
                        .ticketTypeName(item.ticketTypeName())
                        .offerName(item.offerName())
                        .faceValue(item.faceValue())
                        .currency(item.currency())
                        .seatingMode(item.seatingMode())
                        .areaId(item.areaId())
                        .seatId(item.seatId())
                        .totpSecretEnc(encryptedSecret)
                        .status(TicketStatus.ISSUED)
                        .build());
            }
        }

        ticketRepository.saveAll(tickets);   // single bulk insert
        log.info("Issued {} ticket(s) for order {}.", tickets.size(), orderId);
    }
}
