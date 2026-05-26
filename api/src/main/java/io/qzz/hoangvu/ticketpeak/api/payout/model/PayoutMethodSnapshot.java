package io.qzz.hoangvu.ticketpeak.api.payout.model;

import java.util.UUID;

public record PayoutMethodSnapshot(
    UUID payoutMethodId,
    PayoutMethodType type,
    String holderName,
    String bankCode,
    String maskedAccountNumber
) {}
