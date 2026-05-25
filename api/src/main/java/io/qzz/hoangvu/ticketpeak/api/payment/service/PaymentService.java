package io.qzz.hoangvu.ticketpeak.api.payment.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.inventory.service.InventoryService;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.InitiatePaymentRequest;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.InitiatePaymentResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentReReadService paymentReReadService;

    private final String vnpayTmnCode;
    private final String vnpayHashSecret;
    private final String vnpayPayUrl;
    private final String vnpayReturnUrl;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            InventoryService inventoryService,
            ApplicationEventPublisher eventPublisher,
            PaymentReReadService paymentReReadService,
            @Value("${payment.vnpay.tmn-code}") String vnpayTmnCode,
            @Value("${payment.vnpay.hash-secret}") String vnpayHashSecret,
            @Value("${payment.vnpay.pay-url}") String vnpayPayUrl,
            @Value("${payment.vnpay.return-url}") String vnpayReturnUrl
    ) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
        this.paymentReReadService = paymentReReadService;
        this.vnpayTmnCode = vnpayTmnCode;
        this.vnpayHashSecret = vnpayHashSecret;
        this.vnpayPayUrl = vnpayPayUrl;
        this.vnpayReturnUrl = vnpayReturnUrl;
    }

    // ─── Initiate Payment ───────────────────────────────────────────────────

    @Transactional
    public InitiatePaymentResponse initiatePayment(UUID accountId, InitiatePaymentRequest request) {
        Reservation reservation = reservationRepository.findById(request.reservationId())
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

        // Currency provider matrix validation
        if (request.provider() == PaymentProvider.VNPAY && !"VND".equalsIgnoreCase(reservation.getCurrency())) {
            throw PaymentException.unsupportedProvider();
        }
        if (request.provider() == PaymentProvider.STRIPE 
                && !"VND".equalsIgnoreCase(reservation.getCurrency()) 
                && !"USD".equalsIgnoreCase(reservation.getCurrency())) {
            throw PaymentException.unsupportedProvider();
        }

        BigDecimal finalAmount = calculateReservationAmount(reservation);

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
                .provider(request.provider())
                .status(PaymentStatus.PENDING)
                .amount(finalAmount)
                .currency(reservation.getCurrency())
                .gatewayPayload(Map.of())
                .gatewayResponse(Map.of())
                .build();

        Payment saved = paymentRepository.save(payment);
        paymentRepository.flush();

        String checkoutUrl = null;
        String clientSecret = null;

        if (request.provider() == PaymentProvider.VNPAY) {
            checkoutUrl = VnpayCheckoutBuilder.buildRedirectUrl(
                    saved, vnpayTmnCode, vnpayHashSecret, vnpayPayUrl, vnpayReturnUrl);
            saved.setGatewayPayload(Map.of("checkoutUrl", checkoutUrl));
            paymentRepository.save(saved);
        } else {
            clientSecret = StripeCheckoutBuilder.createPaymentIntentSecret(saved);
            saved.setGatewayPayload(Map.of("clientSecret", clientSecret));
            paymentRepository.save(saved);
        }

        return new InitiatePaymentResponse(saved.getId(), checkoutUrl, clientSecret);
    }

    // ─── VNPay authoritative IPN finalization ───────────────────────────────

    @Transactional
    public void finalizeVnpayIpn(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || !VnpayCheckoutBuilder.verifySignature(params, secureHash, vnpayHashSecret)) {
            throw PaymentException.invalidSignature();
        }

        String txnRef = params.get("vnp_TxnRef");
        if (txnRef == null) {
            throw PaymentException.notFound();
        }

        UUID paymentId;
        try {
            paymentId = UUID.fromString(txnRef);
        } catch (IllegalArgumentException e) {
            throw PaymentException.notFound();
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentException::notFound);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("Payment {} already completed. Idempotency success.", paymentId);
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw PaymentException.alreadyCompleted();
        }

        // Pessimistic write lock the reservation row first
        Reservation reservation = reservationRepository.findByIdForUpdate(payment.getReservationId())
                .orElseThrow(PaymentException::reservationNotFound);

        // Amount validation
        String vnpAmountStr = params.get("vnp_Amount");
        if (vnpAmountStr == null) {
            throw PaymentException.amountMismatch();
        }
        BigDecimal gatewayAmount = new BigDecimal(vnpAmountStr)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (gatewayAmount.compareTo(payment.getAmount()) != 0) {
            paymentReReadService.updatePaymentStatusInNewTx(payment.getId(), PaymentStatus.FAILED, new HashMap<>(params));
            throw PaymentException.amountMismatch();
        }

        // Expiry race checking
        if (reservation.getStatus() == ReservationStatus.EXPIRED || reservation.getStatus() == ReservationStatus.CANCELLED) {
            paymentReReadService.updatePaymentStatusInNewTx(payment.getId(), PaymentStatus.FAILED, new HashMap<>(params));
            log.warn("Payment finalization rejected because reservation {} is already {}.",
                    reservation.getId(), reservation.getStatus());
            throw PaymentException.reservationExpired();
        }

        // Atomically finalizes holds and state changes
        String gatewayResponseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if ("00".equals(gatewayResponseCode) && "00".equals(transactionStatus)) {
            // Success
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setGatewayTransactionId(params.get("vnp_TransactionNo"));
            payment.setGatewayResponse(new HashMap<>(params));
            paymentRepository.saveAndFlush(payment);

            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.saveAndFlush(reservation);

            confirmInventoryHolds(reservation);

            // Publish completed event
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
            // Failed from gateway
            payment.setStatus(PaymentStatus.FAILED);
            payment.setGatewayTransactionId(params.get("vnp_TransactionNo"));
            payment.setGatewayResponse(new HashMap<>(params));
            paymentRepository.save(payment);
        }
    }

    // ─── Stripe Webhook authoritative finalization ──────────────────────────

    @Transactional
    public void finalizeStripeWebhook(com.stripe.model.PaymentIntent intent) {
        String paymentIdStr = intent.getMetadata().get("paymentId");
        if (paymentIdStr == null) {
            throw PaymentException.notFound();
        }

        UUID paymentId = UUID.fromString(paymentIdStr);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentException::notFound);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("Payment {} already completed. Webhook idempotency success.", paymentId);
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw PaymentException.alreadyCompleted();
        }

        // Pessimistic write lock the reservation row first
        Reservation reservation = reservationRepository.findByIdForUpdate(payment.getReservationId())
                .orElseThrow(PaymentException::reservationNotFound);

        // Currency-aware smallest unit validation using BigDecimal
        BigDecimal gatewayAmount;
        if ("VND".equalsIgnoreCase(payment.getCurrency())) {
            gatewayAmount = new BigDecimal(intent.getAmount());
        } else {
            gatewayAmount = new BigDecimal(intent.getAmount()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        if (gatewayAmount.compareTo(payment.getAmount()) != 0) {
            paymentReReadService.updatePaymentStatusInNewTx(payment.getId(), PaymentStatus.FAILED, Map.of("amount", intent.getAmount(), "currency", intent.getCurrency()));
            throw PaymentException.amountMismatch();
        }

        // Expiry race checking
        if (reservation.getStatus() == ReservationStatus.EXPIRED || reservation.getStatus() == ReservationStatus.CANCELLED) {
            paymentReReadService.updatePaymentStatusInNewTx(payment.getId(), PaymentStatus.FAILED, Map.of("status", intent.getStatus()));
            log.warn("Stripe webhook rejected finalization because reservation {} is already {}.",
                    reservation.getId(), reservation.getStatus());
            throw PaymentException.reservationExpired();
        }

        log.info("finalizeStripeWebhook: intent status={}, paymentId={}", intent.getStatus(), payment.getId());
        String status = intent.getStatus();
        if ("succeeded".equalsIgnoreCase(status)) {
            log.info("Setting payment status to COMPLETED and saving");
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setGatewayTransactionId(intent.getId());
            payment.setGatewayResponse(Map.of("id", intent.getId(), "status", intent.getStatus()));
            paymentRepository.saveAndFlush(payment);

            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.saveAndFlush(reservation);

            confirmInventoryHolds(reservation);

            // Publish completed event
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
            payment.setGatewayTransactionId(intent.getId());
            payment.setGatewayResponse(Map.of("id", intent.getId(), "status", intent.getStatus()));
            paymentRepository.save(payment);
        }
    }

    // ─── Status Inquiry & Queries ───────────────────────────────────────────

    public PaymentStatusResponse getPaymentStatus(UUID accountId, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndAccountId(paymentId, accountId)
                .orElseThrow(PaymentException::notFound);
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

    // ─── Helpers ────────────────────────────────────────────────────────────

    private BigDecimal calculateReservationAmount(Reservation reservation) {
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
