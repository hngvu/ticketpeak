package io.qzz.hoangvu.ticketpeak.api.resale.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateListingRequest(
        @NotNull UUID ticketId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal askingPrice
) {}
