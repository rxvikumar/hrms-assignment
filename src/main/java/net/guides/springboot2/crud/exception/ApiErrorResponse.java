package net.guides.springboot2.crud.exception;

import java.time.Instant;

/**
 * Standardized API error response (assignment requirement).
 * All error responses follow this exact format:
 * {"error": "ERROR_CODE", "message": "...", "timestamp": "..."}
 */
public class ApiErrorResponse {

    private String error;
    private String message;
    private String timestamp;

    public ApiErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
