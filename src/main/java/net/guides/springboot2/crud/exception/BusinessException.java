package net.guides.springboot2.crud.exception;

/**
 * Custom business exception with an error code.
 * Used for all business rule violations across the system.
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
