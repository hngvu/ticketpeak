package io.qzz.hoangvu.ticketpeak.api.payout.dto;

import io.qzz.hoangvu.ticketpeak.api.payout.model.Payout;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodSnapshot;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayoutResponse(
    UUID id,
    UUID resaleListingId,
    UUID sellerId,
    UUID payoutMethodId,
    PayoutMethodSnapshot payoutMethodSnapshot,
    BigDecimal grossAmount,
    BigDecimal platformFeePercent,
    BigDecimal platformFeeAmount,
    BigDecimal netAmount,
    String currency,
    PayoutStatus status,
    Instant scheduledAfter,
    Instant processedAt,
    String externalRef,
    String note,
    Instant createdAt
) {
    public static PayoutResponse from(Payout p) {
        return new PayoutResponse(
            p.getId(),
            p.getResaleListingId(),
            p.getSellerId(),
            p.getPayoutMethodId(),
            p.getPayoutMethodSnapshot(),
            p.getGrossAmount(),
            p.getPlatformFeePercent(),
            p.getPlatformFeeAmount(),
            p.getNetAmount(),
            p.getCurrency(),
            p.getStatus(),
            p.getScheduledAfter(),
            p.getProcessedAt(),
            p.getExternalRef(),
            p.getNote(),
            p.getCreatedAt()
        );
    }
}
