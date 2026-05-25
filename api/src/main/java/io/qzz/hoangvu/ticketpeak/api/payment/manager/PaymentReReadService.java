package io.qzz.hoangvu.ticketpeak.api.payment.manager;

import io.qzz.hoangvu.ticketpeak.api.payment.exception.PaymentException;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
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
    public void updatePaymentStatusInNewTx(UUID paymentId, PaymentStatus status, Map<String, Object> gatewayResponse) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("updatePaymentStatusInNewTx: payment {} not found", paymentId);
                    return PaymentException.notFound();
                });

        payment.setStatus(status);
        if (gatewayResponse != null) {
            payment.setGatewayResponse(gatewayResponse);
        }
        paymentRepository.saveAndFlush(payment);
    }
}
