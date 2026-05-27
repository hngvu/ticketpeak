package io.qzz.hoangvu.ticketpeak.api.resale.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateResaleConfigRequest(
        @NotNull Boolean resaleEnabled,
        @DecimalMin("100.0") @DecimalMax("500.0") BigDecimal resalePriceCapPercent,
        @NotNull @Min(1) Integer maxResaleListingsPerUser
) {}
