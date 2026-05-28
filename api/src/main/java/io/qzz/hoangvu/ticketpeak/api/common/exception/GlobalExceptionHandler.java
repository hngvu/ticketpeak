package io.qzz.hoangvu.ticketpeak.api.common.exception;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiErrorResponse;
import io.qzz.hoangvu.ticketpeak.api.common.api.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ApiErrorResponse(false, exception.getErrorCode(), exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, List<String>> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse(
                        false,
                        "VALIDATION_FAILED",
                        "Request validation failed",
                        errors,
                        Instant.now()
                ));
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(false, "UNAUTHORIZED", "Authentication is required", Instant.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(false, "FORBIDDEN", "You do not have permission to access this resource", Instant.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(false, "MALFORMED_REQUEST", "Request body is malformed or unreadable", Instant.now()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false, "DATA_INTEGRITY_VIOLATION", "Database constraint violation", Instant.now()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception, HttpServletRequest request) {
        log.debug("Resource not found: {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false, "NOT_FOUND", "The requested resource was not found", Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception processing request {}: {}", request.getRequestURI(), exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(false, "INTERNAL_ERROR", "An unexpected error occurred", Instant.now()));
    }
}
