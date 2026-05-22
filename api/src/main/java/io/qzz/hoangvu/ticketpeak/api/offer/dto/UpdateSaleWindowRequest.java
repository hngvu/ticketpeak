package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UpdateSaleWindowRequest(
        @NotBlank(message = "must not be blank")
        String type,

        @NotNull(message = "must not be null")
        Instant startAt,

        @NotNull(message = "must not be null")
        Instant endAt
) {
}
