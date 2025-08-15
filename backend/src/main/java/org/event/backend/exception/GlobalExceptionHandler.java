package org.event.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.NoSuchElementException;

/**
 * Central place to translate exceptions into consistent JSON responses.
 * Note: 401/403 from Spring Security filters may require custom entry points/handlers
 * in SecurityConfig (exceptionHandling). This handler still helps for @PreAuthorize and others.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // ========== Validation Errors ==========

    /** Bean validation on @RequestBody DTOs */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest req) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY; // 422
        ApiError api = base(status, "Validation failed", req);

        ex.getBindingResult().getFieldErrors().forEach(fe ->
                api.addFieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue())
        );

        return ResponseEntity.status(status).body(api);
    }

    /** Bean validation on query params / path variables */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400
        ApiError api = base(status, "Constraint violation", req);

        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String field = v.getPropertyPath() == null ? null : v.getPropertyPath().toString();
            api.addFieldError(field, v.getMessage(), v.getInvalidValue());
        }
        return ResponseEntity.status(status).body(api);
    }

    // ========== Common Client Errors ==========

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(base(status, "Malformed JSON request", req));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex,
                                                       HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError api = base(status, "Missing request parameter", req);
        api.addFieldError(ex.getParameterName(), "parameter is required", null);
        return ResponseEntity.status(status).body(api);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Parameter '" + ex.getName() + "' has invalid value";
        ApiError api = base(status, msg, req);
        api.addFieldError(ex.getName(), "type mismatch", ex.getValue());
        return ResponseEntity.status(status).body(api);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest req) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(status)
                .body(base(status, "HTTP method not supported", req));
    }

    // ========== Domain & Repository Errors ==========

    /** When service throws explicit not-found (preferred) */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElement(NoSuchElementException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(base(status, nn(ex.getMessage(), "Resource not found"), req));
    }

    /** For bad inputs / illegal state in business logic */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(base(status, nn(ex.getMessage(), "Bad request"), req));
    }

    /** Unique constraints / FK violations, etc. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.CONFLICT; // 409
        return ResponseEntity.status(status)
                .body(base(status, "Data integrity violation", req));
    }

    // ========== Security / Upload ==========

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status)
                .body(base(status, "You do not have permission to access this resource", req));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status)
                .body(base(status, "Invalid email or password", req));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUpload(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.PAYLOAD_TOO_LARGE; // 413
        return ResponseEntity.status(status)
                .body(base(status, "File too large", req));
    }

    // ========== Fallback (last resort) ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError api = base(status, "Unexpected error", req);
        // You can log ex with a correlation id here
        return ResponseEntity.status(status).body(api);
    }

    // ========== Helpers ==========

    private ApiError base(HttpStatus status, String message, HttpServletRequest req) {
        return new ApiError(status.value(), status.getReasonPhrase(), message, req.getRequestURI());
    }

    private String nn(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}
