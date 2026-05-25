package io.qzz.hoangvu.ticketpeak.api.payment.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import io.qzz.hoangvu.ticketpeak.api.payment.exception.PaymentException;
import io.qzz.hoangvu.ticketpeak.api.payment.gateway.StripeCheckoutBuilder;
import io.qzz.hoangvu.ticketpeak.api.payment.manager.FinalizeContext;
import io.qzz.hoangvu.ticketpeak.api.payment.manager.PaymentManager;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class StripeService implements PaymentService, PaymentWebhookHandler {

    private final StripeCheckoutBuilder stripeCheckoutBuilder;
    private final PaymentManager paymentManager;
    private final String stripeWebhookSecret;

    public StripeService(
            StripeCheckoutBuilder stripeCheckoutBuilder,
            PaymentManager paymentManager,
            @Value("${payment.stripe.webhook-secret}") String stripeWebhookSecret
    ) {
        this.stripeCheckoutBuilder = stripeCheckoutBuilder;
        this.paymentManager = paymentManager;
        this.stripeWebhookSecret = stripeWebhookSecret;
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public boolean supportsCurrency(String currency) {
        return "VND".equalsIgnoreCase(currency) || "USD".equalsIgnoreCase(currency);
    }

    @Override
    public String initiateCheckout(Payment payment, String clientIp) {
        return stripeCheckoutBuilder.createPaymentIntentSecret(payment);
    }

    @Override
    public void handleWebhook(WebhookContext context) {
        String sigHeader = context.headers().get("Stripe-Signature");
        if (sigHeader == null) {
            sigHeader = context.headers().get("stripe-signature");
        }
        if (sigHeader == null) {
            throw PaymentException.invalidSignature();
        }

        Event event;
        try {
            event = stripeCheckoutBuilder.verifyStripeWebhook(context.rawBody(), sigHeader, stripeWebhookSecret);
            if (event == null) {
                throw PaymentException.invalidSignature();
            }
        } catch (SignatureVerificationException e) {
            throw PaymentException.invalidSignature();
        }

        String eventType = event.getType();
        if ("payment_intent.succeeded".equalsIgnoreCase(eventType) || "payment_intent.payment_failed".equalsIgnoreCase(eventType)) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (intent == null) {
                throw PaymentException.gatewayError("Stripe webhook deserialization failed");
            }

            String paymentIdStr = intent.getMetadata().get("paymentId");
            if (paymentIdStr == null) {
                throw PaymentException.notFound();
            }

            UUID paymentId;
            try {
                paymentId = UUID.fromString(paymentIdStr);
            } catch (IllegalArgumentException e) {
                throw PaymentException.notFound();
            }

            Payment payment = paymentManager.getPaymentById(paymentId);
            if (payment.getProvider() != PaymentProvider.STRIPE) {
                throw PaymentException.notFound();
            }

            BigDecimal gatewayAmount;
            if ("VND".equalsIgnoreCase(payment.getCurrency())) {
                gatewayAmount = new BigDecimal(intent.getAmount());
            } else {
                gatewayAmount = new BigDecimal(intent.getAmount()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            Map<String, Object> gatewayResponse = Map.of(
                    "id", intent.getId(),
                    "status", intent.getStatus(),
                    "amount", intent.getAmount(),
                    "currency", intent.getCurrency()
            );

            FinalizeContext ctx = paymentManager.validateAndLockForFinalization(
                    paymentId, gatewayAmount, gatewayResponse);

            if (ctx.alreadyCompleted()) {
                log.info("Payment {} already completed. Webhook idempotency success.", paymentId);
                return;
            }

            boolean isSuccess = "succeeded".equalsIgnoreCase(intent.getStatus());
            paymentManager.finalizePayment(
                    ctx.payment(), ctx.reservation(), intent.getId(), gatewayResponse, isSuccess);
        }
    }
}
