package io.qzz.hoangvu.ticketpeak.api.order.dto;

import java.math.BigDecimal;

public record OrderChargeResponse(
        String name,
        String type,
        BigDecimal amount,
        boolean isPercentage
) {}
