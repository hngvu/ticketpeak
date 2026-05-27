package io.qzz.hoangvu.ticketpeak.api.resale.service;

import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.payout.service.PayoutService;
import io.qzz.hoangvu.ticketpeak.api.resale.dto.ResaleOrderResponse;
import io.qzz.hoangvu.ticketpeak.api.resale.exception.ResaleException;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListing;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListingStatus;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleOrder;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleOrderStatus;
import io.qzz.hoangvu.ticketpeak.api.resale.repository.ResaleListingRepository;
import io.qzz.hoangvu.ticketpeak.api.resale.repository.ResaleOrderRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;
import io.qzz.hoangvu.ticketpeak.api.ticket.repository.TicketRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.service.TotpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleOrderService {

    private final ResaleListingRepository listingRepository;
    private final ResaleOrderRepository orderRepository;
    
    // Explicit cross-module dependencies
    private final TicketRepository ticketRepository;
    private final TotpService totpService;
    
    private final EventRepository eventRepository;
    private final PayoutService payoutService;

    @Transactional
    public ResaleOrderResponse initiatePurchase(UUID buyerId, UUID listingId) {
        ResaleListing listing = listingRepository.findByIdForUpdate(listingId)
                .orElseThrow(() -> ResaleException.listingNotFound(listingId));

        if (listing.getStatus() != ResaleListingStatus.ACTIVE) {
            throw ResaleException.listingNotActive();
        }

        if (listing.getSellerId().equals(buyerId)) {
            throw ResaleException.cannotPurchaseOwnListing();
        }

        Event event = eventRepository.findById(listing.getEventId())
                .orElseThrow(() -> ResaleException.listingNotFound(listingId));

        listing.setStatus(ResaleListingStatus.RESERVED);
        listing.setReservedUntil(Instant.now().plus(Duration.ofMinutes(15)));
        listingRepository.save(listing);

        BigDecimal platformFeePercent = event.getServiceFeePercent();

        BigDecimal platformFeeAmount = listing.getAskingPrice().multiply(platformFeePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = listing.getAskingPrice().subtract(platformFeeAmount);

        ResaleOrder order = ResaleOrder.builder()
                .resaleListingId(listing.getId())
                .buyerId(buyerId)
                .askingPrice(listing.getAskingPrice())
                .platformFeePercent(platformFeePercent)
                .platformFeeAmount(platformFeeAmount)
                .netAmount(netAmount)
                .currency(listing.getCurrency())
                .status(ResaleOrderStatus.PENDING)
                .build();

        return ResaleOrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public void onPaymentSuccess(UUID orderId, UUID paymentId) {
        ResaleOrder order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != ResaleOrderStatus.PENDING) return;

        ResaleListing listing = listingRepository.findById(order.getResaleListingId()).orElseThrow();
        listing.setStatus(ResaleListingStatus.SOLD);
        listing.setReservedUntil(null);
        listingRepository.save(listing);

        order.setStatus(ResaleOrderStatus.CONFIRMED);
        order.setPaymentId(paymentId);
        orderRepository.save(order);

        Ticket ticket = ticketRepository.findByIdForUpdate(listing.getTicketId()).orElseThrow();
        ticket.setAccountId(order.getBuyerId());
        
        // ROTATE TOTP SECRET
        String newSecret = totpService.generateSecret();
        ticket.setTotpSecretEnc(totpService.encrypt(newSecret));
        
        ticket.setTransferCount(ticket.getTransferCount() + 1);
        ticketRepository.save(ticket);

        Event event = eventRepository.findById(listing.getEventId()).orElseThrow();

        // Create payout using the gross asking price
        payoutService.createPayout(
                listing.getId(),
                listing.getSellerId(),
                listing.getAskingPrice(),
                order.getPlatformFeePercent(),
                listing.getCurrency(),
                event.getEndAt(),
                event.getStartAt()
        );
    }

    @Transactional
    public void onPaymentFailure(UUID orderId) {
        ResaleOrder order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != ResaleOrderStatus.PENDING) return;

        order.setStatus(ResaleOrderStatus.FAILED);
        orderRepository.save(order);

        ResaleListing listing = listingRepository.findById(order.getResaleListingId()).orElseThrow();
        if (listing.getStatus() == ResaleListingStatus.RESERVED) {
            listing.setStatus(ResaleListingStatus.ACTIVE);
            listing.setReservedUntil(null);
            listingRepository.save(listing);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseExpiredReservation(UUID listingId) {
        ResaleListing listing = listingRepository.findByIdForUpdate(listingId).orElseThrow();
        if (listing.getStatus() == ResaleListingStatus.RESERVED &&
            listing.getReservedUntil() != null &&
            listing.getReservedUntil().isBefore(Instant.now())) {
            
            listing.setStatus(ResaleListingStatus.ACTIVE);
            listing.setReservedUntil(null);
            listingRepository.save(listing);

            orderRepository.findByResaleListingIdAndStatus(listingId, ResaleOrderStatus.PENDING)
                    .ifPresent(order -> {
                        order.setStatus(ResaleOrderStatus.CANCELLED);
                        orderRepository.save(order);
                        log.info("Cancelled expired pending resale order {}", order.getId());
                    });

            log.info("Released expired reservation for resale listing {}", listingId);
        }
    }
}
