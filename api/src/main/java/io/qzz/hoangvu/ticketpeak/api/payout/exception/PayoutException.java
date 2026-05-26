package io.qzz.hoangvu.ticketpeak.api.payout.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import static org.springframework.http.HttpStatus.*;

public final class PayoutException {
    private PayoutException() {}

    public static ApiException methodNotFound() {
        return new ApiException(NOT_FOUND, "PAYOUT_METHOD_NOT_FOUND", "Payout method not found");
    }

    public static ApiException methodInUse() {
        return new ApiException(CONFLICT, "PAYOUT_METHOD_IN_USE", 
                "Cannot delete payout method because it is referenced by active payouts");
    }

    public static ApiException payoutNotFound() {
        return new ApiException(NOT_FOUND, "PAYOUT_NOT_FOUND", "Payout record not found");
    }

    public static ApiException invalidStatusTransition() {
        return new ApiException(CONFLICT, "PAYOUT_INVALID_STATUS_TRANSITION", "Invalid payout status transition");
    }

    public static ApiException payoutAlreadyProcessed() {
        return new ApiException(CONFLICT, "PAYOUT_ALREADY_PROCESSED", "Payout has already been processed");
    }

    public static ApiException noPrimaryMethod() {
        return new ApiException(UNPROCESSABLE_ENTITY, "PAYOUT_NO_PRIMARY_METHOD", 
                "Seller has no primary payout method configured");
    }

    public static ApiException payoutCannotBeCancelled() {
        return new ApiException(CONFLICT, "PAYOUT_CANNOT_BE_CANCELLED", "Only PENDING payouts can be cancelled");
    }
}
