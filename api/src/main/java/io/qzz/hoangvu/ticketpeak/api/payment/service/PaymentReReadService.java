package io.qzz.hoangvu.ticketpeak.api.payment.service;

import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentReReadService {

    private final PaymentRepository paymentRepository;

    public PaymentReReadService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Payment getPaymentInNewTx(UUID paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePaymentStatusInNewTx(UUID paymentId, io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus status, java.util.Map<String, Object> gatewayResponse) {
        paymentRepository.findById(paymentId).ifPresent(p -> {
            p.setStatus(status);
            if (gatewayResponse != null) {
                p.setGatewayResponse(gatewayResponse);
            }
            paymentRepository.saveAndFlush(p);
        });
    }
}
