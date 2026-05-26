package net.guides.springboot2.crud.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler ensuring all errors return structured JSON.
 * No stack traces, no generic 500s (assignment requirement).
 *
 * Handles:
 * - ResourceNotFoundException → 404
 * - BusinessException → mapped by error code (400/409)
 * - MethodArgumentNotValidException → 400 (validation failures)
 * - Everything else → 500 with safe message
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiErrorResponse error = new ApiErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());
        ApiErrorResponse error = new ApiErrorResponse(ex.getErrorCode(), ex.getMessage());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        ApiErrorResponse error = new ApiErrorResponse("VALIDATION_ERROR", message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue();
        ApiErrorResponse error = new ApiErrorResponse("INVALID_PARAMETER", message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiErrorResponse error = new ApiErrorResponse("INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maps business error codes to appropriate HTTP status codes.
     */
    private HttpStatus mapErrorCodeToStatus(String errorCode) {
        switch (errorCode) {
            case "DUPLICATE_CLOCK_IN":
            case "ALREADY_SETTLED":
            case "CURRENT_MONTH_SETTLEMENT":
                return HttpStatus.CONFLICT; // 409
            case "WORKER_NOT_FOUND":
            case "SITE_NOT_FOUND":
            case "ATTENDANCE_NOT_FOUND":
                return HttpStatus.NOT_FOUND; // 404
            default:
                return HttpStatus.BAD_REQUEST; // 400
        }
    }
}
