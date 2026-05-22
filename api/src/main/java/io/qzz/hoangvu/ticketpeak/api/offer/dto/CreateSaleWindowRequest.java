package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateSaleWindowRequest(
        @NotBlank(message = "must not be blank")
        String type,

        @NotNull(message = "must not be null")
        @Future(message = "must be a future date")
        Instant startAt,

        @NotNull(message = "must not be null")
        @Future(message = "must be a future date")
        Instant endAt
) {
}
