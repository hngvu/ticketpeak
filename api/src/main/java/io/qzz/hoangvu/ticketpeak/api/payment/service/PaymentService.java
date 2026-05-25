package io.qzz.hoangvu.ticketpeak.api.payment.service;

import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;

public interface PaymentService {
    PaymentProvider getProvider();
    boolean supportsCurrency(String currency);
    String initiateCheckout(Payment payment, String clientIp);
}
