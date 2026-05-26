package io.qzz.hoangvu.ticketpeak.api.payout.dto;

import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePayoutStatusRequest(
    @NotNull PayoutStatus status,
    String externalRef,
    String note
) {}
