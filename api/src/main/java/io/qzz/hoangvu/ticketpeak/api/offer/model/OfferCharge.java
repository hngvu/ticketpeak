package io.qzz.hoangvu.ticketpeak.api.offer.model;

import java.math.BigDecimal;

public record OfferCharge(
        String name,
        ChargeType type,
        BigDecimal amount,
        boolean isPercentage
) {
}
