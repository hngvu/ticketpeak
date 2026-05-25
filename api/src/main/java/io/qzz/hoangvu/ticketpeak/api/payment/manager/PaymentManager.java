package io.qzz.hoangvu.ticketpeak.api.payment.manager;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.inventory.service.InventoryService;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.PaymentStatusResponse;
import io.qzz.hoangvu.ticketpeak.api.payment.event.PaymentCompletedEvent;
import io.qzz.hoangvu.ticketpeak.api.payment.exception.PaymentException;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.repository.PaymentRepository;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationItem;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import io.qzz.hoangvu.ticketpeak.api.reservation.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PaymentManager {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentReReadService paymentReReadService;

    public PaymentManager(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            InventoryService inventoryService,
            ApplicationEventPublisher eventPublisher,
            PaymentReReadService paymentReReadService
    ) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
        this.paymentReReadService = paymentReReadService;
    }

    public Payment getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(PaymentException::notFound);
    }

    public Reservation getReservationForInitiation(UUID reservationId, UUID accountId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(PaymentException::reservationNotFound);

        if (!reservation.getAccountId().equals(accountId)) {
            throw PaymentException.reservationOwnerMismatch();
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw PaymentException.reservationNotPending();
        }

        if (Instant.now().isAfter(reservation.getExpiresAt())) {
            throw PaymentException.reservationExpired();
        }

        return reservation;
    }

    @Transactional
    public Payment createPendingPayment(Reservation reservation, UUID accountId, PaymentProvider provider, BigDecimal amount) {
        // Cancel any existing pending payment attempts
        List<Payment> pendingAttempts = paymentRepository.findByReservationIdAndStatusIn(
                reservation.getId(), List.of(PaymentStatus.PENDING));
        for (Payment attempt : pendingAttempts) {
            attempt.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(attempt);
        }
        paymentRepository.flush();

        Payment payment = Payment.builder()
                .reservationId(reservation.getId())
                .accountId(accountId)
                .eventId(reservation.getEventId())
                .provider(provider)
                .status(PaymentStatus.PENDING)
                .amount(amount)
                .currency(reservation.getCurrency())
                .gatewayPayload(Map.of())
                .gatewayResponse(Map.of())
                .build();

        Payment saved = paymentRepository.save(payment);
        paymentRepository.flush();
        return saved;
    }

    @Transactional
    public void savePaymentWithGatewayPayload(Payment payment, Map<String, Object> payload) {
        payment.setGatewayPayload(payload);
        paymentRepository.saveAndFlush(payment);
    }

    @Transactional
    public Reservation getReservationForUpdate(UUID reservationId) {
        return reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(PaymentException::reservationNotFound);
    }

    @Transactional
    public FinalizeContext validateAndLockForFinalization(
            UUID paymentId, BigDecimal gatewayAmount, Map<String, Object> gatewayResponse) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentException::notFound);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return FinalizeContext.createAlreadyCompleted();
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw PaymentException.alreadyCompleted();
        }

        // Pessimistic write lock the reservation row first
        Reservation reservation = getReservationForUpdate(payment.getReservationId());

        if (gatewayAmount.compareTo(payment.getAmount()) != 0) {
            paymentReReadService.updatePaymentStatusInNewTx(payment.getId(), PaymentStatus.FAILED, gatewayResponse);
            throw PaymentException.amountMismatch();
        }

        if (reservation.getStatus() == ReservationStatus.EXPIRED || reservation.getStatus() == ReservationStatus.CANCELLED) {
            paymentReReadService.updatePaymentStatusInNewTx(payment.getId(), PaymentStatus.FAILED, gatewayResponse);
            log.warn("Payment finalization rejected because reservation {} is already {}.",
                    reservation.getId(), reservation.getStatus());
            throw PaymentException.reservationExpired();
        }

        return FinalizeContext.of(payment, reservation);
    }

    @Transactional
    public void finalizePayment(
            Payment payment,
            Reservation reservation,
            String gatewayTxId,
            Map<String, Object> gatewayResponse,
            boolean isSuccess
    ) {
        if (isSuccess) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setGatewayTransactionId(gatewayTxId);
            payment.setGatewayResponse(gatewayResponse);
            paymentRepository.saveAndFlush(payment);

            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.saveAndFlush(reservation);

            confirmInventoryHolds(reservation);

            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    this,
                    payment.getId(),
                    reservation.getId(),
                    reservation.getEventId(),
                    reservation.getAccountId(),
                    payment.getAmount(),
                    payment.getCurrency()
            ));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setGatewayTransactionId(gatewayTxId);
            payment.setGatewayResponse(gatewayResponse);
            paymentRepository.save(payment);
        }
    }

    public PaymentStatusResponse getPaymentStatus(UUID accountId, UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentException::notFound);

        if (!payment.getAccountId().equals(accountId)) {
            throw PaymentException.paymentOwnerMismatch();
        }

        return PaymentStatusResponse.from(payment);
    }

    public PaymentStatusResponse getPaymentByReservation(UUID accountId, UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(PaymentException::reservationNotFound);

        if (!reservation.getAccountId().equals(accountId)) {
            throw PaymentException.reservationOwnerMismatch();
        }

        Payment payment = paymentRepository.findFirstByReservationIdOrderByCreatedAtDesc(reservationId)
                .orElseThrow(PaymentException::notFound);

        return PaymentStatusResponse.from(payment);
    }

    public BigDecimal calculateReservationAmount(Reservation reservation) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReservationItem item : reservation.getItems()) {
            BigDecimal unitFaceValue = item.getUnitFaceValue();
            BigDecimal totalChargesPerUnit = BigDecimal.ZERO;
            if (item.getCharges() != null) {
                for (io.qzz.hoangvu.ticketpeak.api.offer.model.OfferCharge charge : item.getCharges()) {
                    BigDecimal chargeAmount;
                    if (charge.isPercentage()) {
                        chargeAmount = unitFaceValue.multiply(charge.amount())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    } else {
                        chargeAmount = charge.amount();
                    }
                    totalChargesPerUnit = totalChargesPerUnit.add(chargeAmount);
                }
            }
            BigDecimal unitTotalPrice = unitFaceValue.add(totalChargesPerUnit);
            BigDecimal itemTotalPrice = unitTotalPrice.multiply(BigDecimal.valueOf(item.getQty()));
            totalAmount = totalAmount.add(itemTotalPrice);
        }
        return totalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private void confirmInventoryHolds(Reservation reservation) {
        for (ReservationItem item : reservation.getItems()) {
            if (item.getSeatingMode() == io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode.GENERAL_ADMISSION) {
                try {
                    inventoryService.confirmGAInventory(
                            reservation.getEventId(), item.getAreaId(), item.getOfferId(), item.getQty());
                } catch (ApiException e) {
                    throw PaymentException.gatewayError("General Admission inventory confirmation failed: " + e.getMessage());
                }
            } else {
                try {
                    inventoryService.sellSeat(reservation.getEventId(), item.getSeatId());
                } catch (ApiException e) {
                    throw PaymentException.gatewayError("Seat inventory sale failed: " + e.getMessage());
                }
            }
        }
    }
}
