package io.qzz.hoangvu.ticketpeak.api.storage.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class StorageException {
    
    private StorageException() {}

    public static ApiException invalidFileType() {
        return new ApiException(
                HttpStatus.BAD_REQUEST,
                "INVALID_FILE_TYPE",
                "The requested file type is not supported. Only images (JPEG, PNG, WEBP, GIF) are allowed."
        );
    }

    public static ApiException presignedUrlGenerationFailed(String message) {
        return new ApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "STORAGE_PRESIGNED_URL_FAILED",
                "Failed to generate presigned upload URL: " + message
        );
    }
}
