package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.ChargeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ChargeRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String name,
        @NotNull(message = "must not be null")
        ChargeType type,
        @NotNull(message = "must not be null")
        @PositiveOrZero(message = "must be greater than or equal to 0")
        BigDecimal amount,
        boolean isPercentage
) {
}
