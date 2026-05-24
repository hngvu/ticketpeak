package io.qzz.hoangvu.ticketpeak.api.auth.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class AuthException {
    private AuthException() {}

    public static ApiException invalidCredentials(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", message);
    }

    public static ApiException invalidRefreshToken() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid");
    }

    public static ApiException refreshTokenRevoked() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REVOKED", "Refresh token is no longer valid");
    }

    public static ApiException accountInactive() {
        return new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE", "Account is not active");
    }
}
