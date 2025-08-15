package org.event.backend.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard API error payload returned to the frontend.
 */
public class ApiError {
    private int status;                 // HTTP status code (e.g., 400, 404)
    private String error;               // HTTP reason phrase (e.g., "Bad Request")
    private String message;             // Human-friendly message
    private String path;                // Request URI
    private Instant timestamp;          // When the error happened
    private List<ApiFieldError> errors; // Optional: per-field validation errors

    public ApiError() {
        this.timestamp = Instant.now();
        this.errors = new ArrayList<>();
    }

    public ApiError(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public void addFieldError(ApiFieldError fieldError) {
        this.errors.add(fieldError);
    }

    public void addFieldError(String field, String message, Object rejectedValue) {
        this.errors.add(new ApiFieldError(field, message, rejectedValue));
    }

    // Getters / Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public List<ApiFieldError> getErrors() { return errors; }
    public void setErrors(List<ApiFieldError> errors) { this.errors = errors; }
}
