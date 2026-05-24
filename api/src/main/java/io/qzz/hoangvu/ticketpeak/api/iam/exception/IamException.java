package io.qzz.hoangvu.ticketpeak.api.iam.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class IamException {
    private IamException() {}

    public static ApiException permissionNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "PERMISSION_NOT_FOUND", "Permission not found");
    }

    public static ApiException permissionAlreadyExists() {
        return new ApiException(HttpStatus.CONFLICT, "PERMISSION_ALREADY_EXISTS", "Permission code already exists");
    }

    public static ApiException permissionAlreadyGranted() {
        return new ApiException(HttpStatus.CONFLICT, "PERMISSION_ALREADY_GRANTED", "Account already has this permission in the organization");
    }

    public static ApiException permissionGrantNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "PERMISSION_GRANT_NOT_FOUND", "Permission grant not found or already revoked");
    }

    public static ApiException invalidPermissionScope() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PERMISSION_SCOPE", "Can only grant organization-scoped permissions");
    }
}
