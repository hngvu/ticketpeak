package io.qzz.hoangvu.ticketpeak.api.common.api;

import java.time.Instant;

public record ApiErrorResponse(
        boolean success,
        String error,
        String message,
        Instant timestamp
) {
}
