package io.qzz.hoangvu.ticketpeak.api.payment.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.InitiatePaymentRequest;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.InitiatePaymentResponse;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.PaymentStatusResponse;
import io.qzz.hoangvu.ticketpeak.api.payment.exception.PaymentException;
import io.qzz.hoangvu.ticketpeak.api.payment.gateway.VnpayCheckoutBuilder;
import io.qzz.hoangvu.ticketpeak.api.payment.manager.PaymentManager;
import io.qzz.hoangvu.ticketpeak.api.payment.manager.PaymentOrchestrator;
import io.qzz.hoangvu.ticketpeak.api.payment.manager.PaymentReReadService;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.service.PaymentWebhookHandler;
import io.qzz.hoangvu.ticketpeak.api.payment.service.WebhookContext;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentManager paymentManager;
    private final PaymentOrchestrator paymentOrchestrator;
    private final List<PaymentWebhookHandler> webhookHandlers;
    private final VnpayCheckoutBuilder vnpayCheckoutBuilder;
    private final PaymentReReadService paymentReReadService;
    private final String vnpayHashSecret;

    public PaymentController(
            PaymentManager paymentManager,
            PaymentOrchestrator paymentOrchestrator,
            List<PaymentWebhookHandler> webhookHandlers,
            VnpayCheckoutBuilder vnpayCheckoutBuilder,
            PaymentReReadService paymentReReadService,
            @Value("${payment.vnpay.hash-secret}") String vnpayHashSecret
    ) {
        this.paymentManager = paymentManager;
        this.paymentOrchestrator = paymentOrchestrator;
        this.webhookHandlers = webhookHandlers;
        this.vnpayCheckoutBuilder = vnpayCheckoutBuilder;
        this.paymentReReadService = paymentReReadService;
        this.vnpayHashSecret = vnpayHashSecret;
    }

    // ─── Initiate Payment ───────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody InitiatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String clientIp = httpServletRequest.getHeader("X-Forwarded-For") != null
                ? httpServletRequest.getHeader("X-Forwarded-For").split(",")[0].trim()
                : httpServletRequest.getRemoteAddr();

        InitiatePaymentResponse response = paymentOrchestrator.initiatePayment(account.accountId(), request, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment checkout initiated"));
    }

    // ─── Get Payment Status ─────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id
    ) {
        PaymentStatusResponse response = paymentManager.getPaymentStatus(account.accountId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── Get Payment Status by Reservation ──────────────────────────────────

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentByReservation(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID reservationId
    ) {
        PaymentStatusResponse response = paymentManager.getPaymentByReservation(account.accountId(), reservationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── VNPay Return URL (Read-only status lookup) ─────────────────────────

    @GetMapping("/vnpay/return")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> vnpayReturn(
            @RequestParam Map<String, String> params
    ) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || !vnpayCheckoutBuilder.verifySignature(params, secureHash, vnpayHashSecret)) {
            throw PaymentException.invalidSignature();
        }

        String txnRef = params.get("vnp_TxnRef");
        if (txnRef == null) {
            throw PaymentException.notFound();
        }

        UUID paymentId = UUID.fromString(txnRef);
        Payment payment = paymentReReadService.getPaymentInNewTx(paymentId);
        if (payment == null) {
            throw PaymentException.notFound();
        }
        return ResponseEntity.ok(ApiResponse.success(PaymentStatusResponse.from(payment)));
    }

    // ─── VNPay Server IPN Callback (Public server-to-server) ────────────────

    @PostMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        log.info("Incoming VNPay IPN params: {}", params);
        Map<String, String> response = new HashMap<>();

        try {
            PaymentWebhookHandler handler = webhookHandlers.stream()
                    .filter(h -> h.getProvider() == PaymentProvider.VNPAY)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("VNPay webhook handler not found"));

            handler.handleWebhook(new WebhookContext(params, null, null));
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
            throw e;

        } catch (Exception e) {
            log.error("Unhandled VNPay IPN system failure", e);
            response.put("RspCode", "99");
            response.put("Message", "Input data required");
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
        
        try {
            PaymentWebhookHandler handler = webhookHandlers.stream()
                    .filter(h -> h.getProvider() == PaymentProvider.STRIPE)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Stripe webhook handler not found"));

            handler.handleWebhook(new WebhookContext(null, payload, Map.of("Stripe-Signature", sigHeader)));
            return ResponseEntity.ok().build();

        } catch (ApiException e) {
            if ("PAYMENT_INVALID_SIGNATURE".equals(e.getErrorCode())) {
                log.warn("Stripe webhook signature validation failed: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            if ("PAYMENT_ALREADY_COMPLETED".equals(e.getErrorCode())) {
                return ResponseEntity.ok().build();
            }
            throw e;

        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            log.warn("Stripe Webhook concurrency clash: {}", e.getMessage());
            // Since we don't have the intent deserialized here directly, let's recover by capturing transactionId later or just fail for retry.
            // But wait, Stripe will retry on non-2xx, which is desired!
            throw e;
        }
    }
}
