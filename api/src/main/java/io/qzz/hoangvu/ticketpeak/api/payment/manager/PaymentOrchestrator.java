package io.qzz.hoangvu.ticketpeak.api.payment.manager;

import io.qzz.hoangvu.ticketpeak.api.payment.dto.InitiatePaymentRequest;
import io.qzz.hoangvu.ticketpeak.api.payment.dto.InitiatePaymentResponse;
import io.qzz.hoangvu.ticketpeak.api.payment.exception.PaymentException;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import io.qzz.hoangvu.ticketpeak.api.payment.service.PaymentService;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentOrchestrator {

    private final PaymentManager paymentManager;
    private final List<PaymentService> paymentServices;

    public PaymentOrchestrator(
            PaymentManager paymentManager,
            List<PaymentService> paymentServices
    ) {
        this.paymentManager = paymentManager;
        this.paymentServices = paymentServices;
    }

    public InitiatePaymentResponse initiatePayment(UUID accountId, InitiatePaymentRequest request, String clientIp) {
        Reservation reservation = paymentManager.getReservationForInitiation(request.reservationId(), accountId);

        // Find matching provider service that supports the currency
        PaymentService paymentService = paymentServices.stream()
                .filter(s -> s.getProvider() == request.provider() && s.supportsCurrency(reservation.getCurrency()))
                .findFirst()
                .orElseThrow(PaymentException::unsupportedProvider);

        BigDecimal finalAmount = paymentManager.calculateReservationAmount(reservation);

        // Create the pending payment (cancels previous ones)
        Payment pendingPayment = paymentManager.createPendingPayment(
                reservation, accountId, request.provider(), finalAmount);

        // Initiate the gateway checkout process
        String checkoutResult = paymentService.initiateCheckout(pendingPayment, clientIp);

        String checkoutUrl = null;
        String clientSecret = null;

        if (request.provider() == PaymentProvider.VNPAY) {
            checkoutUrl = checkoutResult;
            paymentManager.savePaymentWithGatewayPayload(pendingPayment, Map.of("checkoutUrl", checkoutUrl));
        } else {
            clientSecret = checkoutResult;
            paymentManager.savePaymentWithGatewayPayload(pendingPayment, Map.of("clientSecret", clientSecret));
        }

        return new InitiatePaymentResponse(pendingPayment.getId(), checkoutUrl, clientSecret);
    }
}
