package io.qzz.hoangvu.ticketpeak.api.payment.gateway;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import io.qzz.hoangvu.ticketpeak.api.payment.exception.PaymentException;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StripeCheckoutBuilder {

    public String createPaymentIntentSecret(Payment payment) {
        long stripeAmount;
        if ("VND".equalsIgnoreCase(payment.getCurrency())) {
            // VND is zero-decimal
            stripeAmount = payment.getAmount().longValue();
        } else {
            // USD is fractional (2 decimals)
            stripeAmount = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(stripeAmount)
                .setCurrency(payment.getCurrency().toLowerCase())
                .putMetadata("paymentId", payment.getId().toString())
                .putMetadata("reservationId", payment.getReservationId().toString())
                .build();

        try {
            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();
        } catch (Exception e) {
            throw PaymentException.gatewayError("Stripe PaymentIntent creation failed: " + e.getMessage());
        }
    }

    public Event verifyStripeWebhook(String payload, String signatureHeader, String webhookSecret)
            throws SignatureVerificationException {
        return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    }
}
