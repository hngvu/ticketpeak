package io.qzz.hoangvu.ticketpeak.api.common.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ValidationErrorResponse(
        boolean success,
        String error,
        String message,
        Map<String, List<String>> errors,
        Instant timestamp
) {
}
