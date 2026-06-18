package io.qzz.hoangvu.ticketpeak.api.payment.service;

import io.qzz.hoangvu.ticketpeak.api.payment.exception.PaymentException;
import io.qzz.hoangvu.ticketpeak.api.payment.gateway.VnpayCheckoutBuilder;
import io.qzz.hoangvu.ticketpeak.api.payment.manager.FinalizeContext;
import io.qzz.hoangvu.ticketpeak.api.payment.manager.PaymentManager;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class VnpayService implements PaymentService, PaymentWebhookHandler {

    private final VnpayCheckoutBuilder vnpayCheckoutBuilder;
    private final PaymentManager paymentManager;

    private final String vnpayTmnCode;
    private final String vnpayHashSecret;
    private final String vnpayPayUrl;
    private final String vnpayReturnUrl;

    public VnpayService(
            VnpayCheckoutBuilder vnpayCheckoutBuilder,
            PaymentManager paymentManager,
            @Value("${payment.vnpay.tmn-code}") String vnpayTmnCode,
            @Value("${payment.vnpay.hash-secret}") String vnpayHashSecret,
            @Value("${payment.vnpay.pay-url}") String vnpayPayUrl,
            @Value("${payment.vnpay.return-url}") String vnpayReturnUrl
    ) {
        this.vnpayCheckoutBuilder = vnpayCheckoutBuilder;
        this.paymentManager = paymentManager;
        this.vnpayTmnCode = vnpayTmnCode;
        this.vnpayHashSecret = vnpayHashSecret;
        this.vnpayPayUrl = vnpayPayUrl;
        this.vnpayReturnUrl = vnpayReturnUrl;
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.VNPAY;
    }

    @Override
    public boolean supportsCurrency(String currency) {
        return "VND".equalsIgnoreCase(currency);
    }

    @Override
    @Retry(name = "externalPaymentAPI")
    public String initiateCheckout(Payment payment, String clientIp) {
        return vnpayCheckoutBuilder.buildRedirectUrl(
                payment, vnpayTmnCode, vnpayHashSecret, vnpayPayUrl, vnpayReturnUrl, clientIp);
    }

    @Override
    public void handleWebhook(WebhookContext context) {
        Map<String, String> params = context.params();
        if (params == null || params.isEmpty()) {
            throw PaymentException.invalidSignature();
        }

        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || !vnpayCheckoutBuilder.verifySignature(params, secureHash, vnpayHashSecret)) {
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

        // Retrieve and check provider matches VNPAY
        Payment payment = paymentManager.getPaymentById(paymentId);
        if (payment.getProvider() != PaymentProvider.VNPAY) {
            throw PaymentException.notFound();
        }

        // Amount parsing & validation
        String vnpAmountStr = params.get("vnp_Amount");
        if (vnpAmountStr == null) {
            throw PaymentException.amountMismatch();
        }

        BigDecimal gatewayAmount;
        try {
            gatewayAmount = new BigDecimal(vnpAmountStr)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw PaymentException.amountMismatch();
        }

        FinalizeContext ctx = paymentManager.validateAndLockForFinalization(
                paymentId, gatewayAmount, new HashMap<>(params));

        if (ctx.alreadyCompleted()) {
            log.info("Payment {} already completed. Idempotency success.", paymentId);
            return;
        }

        String gatewayResponseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        boolean isSuccess = "00".equals(gatewayResponseCode) && "00".equals(transactionStatus);
        String gatewayTxId = params.get("vnp_TransactionNo");

        paymentManager.finalizePayment(
                ctx.payment(), ctx.reservation(), gatewayTxId, new HashMap<>(params), isSuccess);
    }
}
