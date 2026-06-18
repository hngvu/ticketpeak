package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.SaleWindowType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateSaleWindowRequest(
        @NotNull(message = "must not be null")
        SaleWindowType type,

        @NotNull(message = "must not be null")
        Instant startAt,

        @NotNull(message = "must not be null")
        Instant endAt,

        String accessCode
) {
}
