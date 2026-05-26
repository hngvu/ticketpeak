package io.qzz.hoangvu.ticketpeak.api.payout.dto;

import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePayoutMethodRequest(
    @NotNull PayoutMethodType type,
    @NotBlank String holderName,
    String bankCode,
    @NotBlank String accountNumber,
    boolean isPrimary
) {}
