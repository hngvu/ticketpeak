package io.qzz.hoangvu.ticketpeak.api.payout.dto;

import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethod;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodStatus;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodType;
import java.time.Instant;
import java.util.UUID;

public record PayoutMethodResponse(
    UUID id,
    PayoutMethodType type,
    boolean isPrimary,
    String holderName,
    String bankCode,
    String maskedAccountNumber,
    PayoutMethodStatus status,
    Instant verifiedAt,
    Instant createdAt
) {
    public static PayoutMethodResponse from(PayoutMethod pm, String plainAccountNumber) {
        String masked = maskAccountNumber(plainAccountNumber);
        return new PayoutMethodResponse(
            pm.getId(),
            pm.getType(),
            pm.isPrimary(),
            pm.getHolderName(),
            pm.getBankCode(),
            masked,
            pm.getStatus(),
            pm.getVerifiedAt(),
            pm.getCreatedAt()
        );
    }

    public static String maskAccountNumber(String plainNumber) {
        if (plainNumber == null || plainNumber.length() < 4) return "****";
        return "****" + plainNumber.substring(plainNumber.length() - 4);
    }
}
