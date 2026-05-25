package io.qzz.hoangvu.ticketpeak.api.payment.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.InitiatePaymentRequest;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.InitiatePaymentResponse;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.PaymentStatusResponse;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.service.PaymentReReadService;
import io.qzz.hoangvu.ticketpeak.api.payment.service.PaymentService;
import io.qzz.hoangvu.ticketpeak.api.payment.service.StripeCheckoutBuilder;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentReReadService paymentReReadService;
    private final String stripeWebhookSecret;

    public PaymentController(
            PaymentService paymentService,
            PaymentReReadService paymentReReadService,
            @Value("${payment.stripe.webhook-secret}") String stripeWebhookSecret
    ) {
        this.paymentService = paymentService;
        this.paymentReReadService = paymentReReadService;
        this.stripeWebhookSecret = stripeWebhookSecret;
    }

    // ─── Initiate Payment ───────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody InitiatePaymentRequest request
    ) {
        InitiatePaymentResponse response = paymentService.initiatePayment(account.accountId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment checkout initiated"));
    }

    // ─── Get Payment Status ─────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id
    ) {
        PaymentStatusResponse response = paymentService.getPaymentStatus(account.accountId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── Get Payment Status by Reservation ──────────────────────────────────

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentByReservation(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID reservationId
    ) {
        PaymentStatusResponse response = paymentService.getPaymentByReservation(account.accountId(), reservationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── VNPay Return URL (Read-only status lookup) ─────────────────────────

    @GetMapping("/vnpay/return")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> vnpayReturn(
            @RequestParam("vnp_TxnRef") String txnRef
    ) {
        UUID paymentId = UUID.fromString(txnRef);
        // Uses the public unauthenticated lookup mapping for UI display
        Payment payment = paymentReReadService.getPaymentInNewTx(paymentId);
        if (payment == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Payment record not found");
        }
        return ResponseEntity.ok(ApiResponse.success(PaymentStatusResponse.from(payment)));
    }

    // ─── VNPay Server IPN Callback (Public server-to-server) ────────────────

    @PostMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        log.info("Incoming VNPay IPN params: {}", params);
        Map<String, String> response = new HashMap<>();

        try {
            paymentService.finalizeVnpayIpn(params);
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return ResponseEntity.ok(response);

        } catch (ApiException e) {
            log.warn("VNPay IPN error (ApiException): code={}, message={}", e.getErrorCode(), e.getMessage());
            switch (e.getErrorCode()) {
                case "PAYMENT_INVALID_SIGNATURE":
                    response.put("RspCode", "97");
                    response.put("Message", "Invalid signature");
                    break;
                case "RESERVATION_NOT_FOUND":
                case "PAYMENT_NOT_FOUND":
                    response.put("RspCode", "01");
                    response.put("Message", "Order not found");
                    break;
                case "PAYMENT_AMOUNT_MISMATCH":
                    response.put("RspCode", "04");
                    response.put("Message", "Invalid amount");
                    break;
                case "PAYMENT_ALREADY_COMPLETED":
                case "RESERVATION_ALREADY_FINALIZED":
                    response.put("RspCode", "02");
                    response.put("Message", "Order already confirmed");
                    break;
                default:
                    response.put("RspCode", "99");
                    response.put("Message", "Input data required");
                    break;
            }
            return ResponseEntity.ok(response);

        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            log.warn("VNPay IPN concurrency clash: {}", e.getMessage());
            String txnRef = params.get("vnp_TxnRef");
            if (txnRef != null) {
                try {
                    UUID paymentId = UUID.fromString(txnRef);
                    Payment current = paymentReReadService.getPaymentInNewTx(paymentId);
                    if (current != null && current.getStatus() == PaymentStatus.COMPLETED) {
                        response.put("RspCode", "02");
                        response.put("Message", "Order already confirmed");
                        return ResponseEntity.ok(response);
                    }
                } catch (Exception ex) {
                    log.error("Failed to re-read payment state during concurrency recover", ex);
                }
            }
            // Propagate exception to return non-2xx status and trigger VNPay retry
            throw e;

        } catch (Exception e) {
            log.error("Unhandled VNPay IPN system failure", e);
            response.put("RspCode", "99");
            response.put("Message", "Input data required");
            // Propagate exception to return non-2xx status and trigger VNPay retry
            throw new RuntimeException(e);
        }
    }

    // ─── Stripe Server Webhook Callback (Public server-to-server) ───────────

    @PostMapping("/stripe/webhook")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        log.info("Incoming Stripe webhook payload length: {}", payload.length());
        Event event;
        try {
            event = StripeCheckoutBuilder.verifyStripeWebhook(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String eventType = event.getType();
        if ("payment_intent.succeeded".equalsIgnoreCase(eventType) || "payment_intent.payment_failed".equalsIgnoreCase(eventType)) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (intent == null) {
                log.warn("Failed to deserialize PaymentIntent object from Stripe payload");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            String paymentIdStr = intent.getMetadata().get("paymentId");
            try {
                paymentService.finalizeStripeWebhook(intent);
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
                log.warn("Stripe Webhook concurrency clash: {}", e.getMessage());
                if (paymentIdStr != null) {
                    try {
                        UUID paymentId = UUID.fromString(paymentIdStr);
                        Payment current = paymentReReadService.getPaymentInNewTx(paymentId);
                        if (current != null && current.getStatus() == PaymentStatus.COMPLETED) {
                            return ResponseEntity.ok().build();
                        }
                    } catch (Exception ex) {
                        log.error("Failed to re-read payment status during Stripe Webhook recovery", ex);
                    }
                }
                // Propagate to trigger retry
                throw e;
            } catch (ApiException e) {
                // If it is a finalization rejection due to already completed, return 200 so Stripe stops retrying
                if ("PAYMENT_ALREADY_COMPLETED".equals(e.getErrorCode())) {
                    return ResponseEntity.ok().build();
                }
                // For other business errors (like mismatch or expired reservation), let Stripe retry or raise 500
                throw e;
            }
        }

        return ResponseEntity.ok().build();
    }
}
