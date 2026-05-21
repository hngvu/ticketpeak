package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.ChargeType;
import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferCharge;

import java.math.BigDecimal;

public record OfferChargeResponse(
        String name,
        ChargeType type,
        BigDecimal amount,
        boolean isPercentage
) {
    public static OfferChargeResponse from(OfferCharge charge) {
        return new OfferChargeResponse(charge.name(), charge.type(), charge.amount(), charge.isPercentage());
    }
}
