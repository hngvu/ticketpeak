package io.qzz.hoangvu.ticketpeak.api.payout.service;

import io.qzz.hoangvu.ticketpeak.api.common.crypto.EncryptionService;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.PayoutMethodResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.PayoutResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.UpdatePayoutStatusRequest;
import io.qzz.hoangvu.ticketpeak.api.payout.exception.PayoutException;
import io.qzz.hoangvu.ticketpeak.api.payout.model.*;
import io.qzz.hoangvu.ticketpeak.api.payout.repository.PayoutMethodRepository;
import io.qzz.hoangvu.ticketpeak.api.payout.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final PayoutMethodRepository payoutMethodRepository;
    private final EncryptionService encryptionService;

    private static final java.util.Map<PayoutStatus, java.util.Set<PayoutStatus>> VALID_TRANSITIONS = java.util.Map.of(
            PayoutStatus.PENDING, java.util.Set.of(PayoutStatus.PROCESSING, PayoutStatus.CANCELLED),
            PayoutStatus.PROCESSING, java.util.Set.of(PayoutStatus.PAID, PayoutStatus.FAILED)
    );

    @Transactional
    public PayoutResponse createPayout(UUID resaleListingId, UUID sellerId, BigDecimal grossAmount,
                                       BigDecimal platformFeePercent, String currency, Instant eventEndAt, Instant eventStartAt) {
        PayoutMethod pm = payoutMethodRepository.findByAccountIdAndIsPrimaryTrueAndStatus(sellerId, PayoutMethodStatus.ACTIVE)
                .orElseThrow(PayoutException::noPrimaryMethod);

        BigDecimal platformFeeAmount = grossAmount.multiply(platformFeePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = grossAmount.subtract(platformFeeAmount);

        Instant scheduledAfter = (eventEndAt != null) ? eventEndAt.plus(Duration.ofDays(3))
                : (eventStartAt != null ? eventStartAt.plus(Duration.ofDays(1)) : Instant.now().plus(Duration.ofDays(3)));

        String plainAccountNumber = encryptionService.decrypt(pm.getAccountNumberEnc());
        String masked = PayoutMethodResponse.maskAccountNumber(plainAccountNumber);

        PayoutMethodSnapshot snapshot = new PayoutMethodSnapshot(
                pm.getId(),
                pm.getType(),
                pm.getHolderName(),
                pm.getBankCode(),
                masked
        );

        Payout payout = Payout.builder()
                .resaleListingId(resaleListingId)
                .sellerId(sellerId)
                .payoutMethodId(pm.getId())
                .payoutMethodSnapshot(snapshot)
                .grossAmount(grossAmount)
                .platformFeePercent(platformFeePercent)
                .platformFeeAmount(platformFeeAmount)
                .netAmount(netAmount)
                .currency(currency)
                .status(PayoutStatus.PENDING)
                .scheduledAfter(scheduledAfter)
                .build();

        payoutRepository.save(payout);
        return PayoutResponse.from(payout);
    }

    @Transactional(readOnly = true)
    public List<UUID> getEligiblePayoutIds(Instant before) {
        return payoutRepository.findIdsByStatusAndScheduledAfterLessThanEqual(PayoutStatus.PENDING, before);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releasePayout(UUID id) {
        Payout payout = payoutRepository.findById(id)
                .orElseThrow(PayoutException::payoutNotFound);

        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw PayoutException.invalidStatusTransition();
        }

        payout.setStatus(PayoutStatus.PROCESSING);
        payoutRepository.save(payout);
        log.info("Transitioned payout {} to PROCESSING.", id);
    }

    @Transactional
    public PayoutResponse updatePayoutStatus(UUID payoutId, UpdatePayoutStatusRequest req) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(PayoutException::payoutNotFound);

        if (payout.getStatus() == PayoutStatus.PAID || payout.getStatus() == PayoutStatus.CANCELLED) {
            throw PayoutException.payoutAlreadyProcessed();
        }

        if (!VALID_TRANSITIONS.getOrDefault(payout.getStatus(), java.util.Set.of()).contains(req.status())) {
            throw PayoutException.invalidStatusTransition();
        }

        payout.setStatus(req.status());
        if (req.status() == PayoutStatus.PAID) {
            payout.setProcessedAt(Instant.now());
        }
        if (req.externalRef() != null) {
            payout.setExternalRef(req.externalRef());
        }
        if (req.note() != null) {
            payout.setNote(req.note());
        }

        payoutRepository.save(payout);
        return PayoutResponse.from(payout);
    }

    @Transactional
    public void cancelPayout(UUID resaleListingId) {
        Payout payout = payoutRepository.findByResaleListingId(resaleListingId)
                .orElseThrow(PayoutException::payoutNotFound);

        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw PayoutException.payoutCannotBeCancelled();
        }

        payout.setStatus(PayoutStatus.CANCELLED);
        payoutRepository.save(payout);
    }

    @Transactional(readOnly = true)
    public Page<PayoutResponse> listPayoutsForSeller(UUID sellerId, Pageable pageable) {
        return payoutRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable)
                .map(PayoutResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PayoutResponse> listPayoutsForAdmin(PayoutStatus status, Instant scheduledBefore, Pageable pageable) {
        if (status != null && scheduledBefore != null) {
            return payoutRepository.findByStatusAndScheduledAfterLessThanEqual(status, scheduledBefore, pageable)
                    .map(PayoutResponse::from);
        } else if (status != null) {
            return payoutRepository.findByStatus(status, pageable)
                    .map(PayoutResponse::from);
        } else {
            return payoutRepository.findAll(pageable)
                    .map(PayoutResponse::from);
        }
    }

    @Transactional(readOnly = true)
    public PayoutResponse getPayoutForAdmin(UUID payoutId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(PayoutException::payoutNotFound);
        return PayoutResponse.from(payout);
    }

}
