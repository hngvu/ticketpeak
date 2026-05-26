package io.qzz.hoangvu.ticketpeak.api.payout.service;

import io.qzz.hoangvu.ticketpeak.api.common.crypto.EncryptionService;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.CreatePayoutMethodRequest;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.PayoutMethodResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.exception.PayoutException;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethod;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodStatus;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodType;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutStatus;
import io.qzz.hoangvu.ticketpeak.api.payout.repository.PayoutMethodRepository;
import io.qzz.hoangvu.ticketpeak.api.payout.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayoutMethodService {

    private final PayoutMethodRepository payoutMethodRepository;
    private final PayoutRepository payoutRepository;
    private final EncryptionService encryptionService;

    @Transactional
    public PayoutMethodResponse addMethod(UUID accountId, CreatePayoutMethodRequest req) {
        if (req.type() == PayoutMethodType.BANK_TRANSFER && (req.bankCode() == null || req.bankCode().isBlank())) {
            throw new IllegalArgumentException("bankCode is required for BANK_TRANSFER");
        }

        String encryptedAccountNumber = encryptionService.encrypt(req.accountNumber());

        if (req.isPrimary()) {
            payoutMethodRepository.findByAccountIdAndIsPrimaryTrueAndStatus(accountId, PayoutMethodStatus.ACTIVE)
                    .ifPresent(oldPrimary -> {
                        oldPrimary.setPrimary(false);
                        payoutMethodRepository.saveAndFlush(oldPrimary);
                    });
        }

        PayoutMethod pm = PayoutMethod.builder()
                .accountId(accountId)
                .type(req.type())
                .isPrimary(req.isPrimary())
                .holderName(req.holderName())
                .bankCode(req.type() == PayoutMethodType.BANK_TRANSFER ? req.bankCode() : null)
                .accountNumberEnc(encryptedAccountNumber)
                .status(PayoutMethodStatus.ACTIVE)
                .build();

        payoutMethodRepository.save(pm);
        return PayoutMethodResponse.from(pm, req.accountNumber());
    }

    @Transactional
    public void setPrimary(UUID accountId, UUID methodId) {
        PayoutMethod target = payoutMethodRepository.findByIdAndAccountId(methodId, accountId)
                .orElseThrow(PayoutException::methodNotFound);

        if (target.getStatus() != PayoutMethodStatus.ACTIVE) {
            throw PayoutException.methodNotFound();
        }

        payoutMethodRepository.findByAccountIdAndIsPrimaryTrueAndStatus(accountId, PayoutMethodStatus.ACTIVE)
                .ifPresent(oldPrimary -> {
                    oldPrimary.setPrimary(false);
                    payoutMethodRepository.saveAndFlush(oldPrimary);
                });

        target.setPrimary(true);
        payoutMethodRepository.save(target);
    }

    @Transactional
    public void removeMethod(UUID accountId, UUID methodId) {
        PayoutMethod target = payoutMethodRepository.findByIdAndAccountId(methodId, accountId)
                .orElseThrow(PayoutException::methodNotFound);

        if (target.getStatus() != PayoutMethodStatus.ACTIVE) {
            throw PayoutException.methodNotFound();
        }

        // Guard against removing payout methods referenced by active payouts (PENDING, PROCESSING)
        boolean inUse = !payoutRepository.findByPayoutMethodIdAndStatusIn(
                methodId, List.of(PayoutStatus.PENDING, PayoutStatus.PROCESSING)).isEmpty();
        if (inUse) {
            throw PayoutException.methodInUse();
        }

        target.setStatus(PayoutMethodStatus.REMOVED);
        target.setPrimary(false);
        payoutMethodRepository.save(target);
    }

    @Transactional(readOnly = true)
    public List<PayoutMethodResponse> listMethods(UUID accountId) {
        return payoutMethodRepository.findByAccountIdAndStatus(accountId, PayoutMethodStatus.ACTIVE)
                .stream()
                .map(pm -> {
                    String plainNumber = encryptionService.decrypt(pm.getAccountNumberEnc());
                    return PayoutMethodResponse.from(pm, plainNumber);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PayoutMethodResponse getMethod(UUID accountId, UUID methodId) {
        PayoutMethod pm = payoutMethodRepository.findByIdAndAccountId(methodId, accountId)
                .orElseThrow(PayoutException::methodNotFound);
        if (pm.getStatus() != PayoutMethodStatus.ACTIVE) {
            throw PayoutException.methodNotFound();
        }
        String plainNumber = encryptionService.decrypt(pm.getAccountNumberEnc());
        return PayoutMethodResponse.from(pm, plainNumber);
    }
}
