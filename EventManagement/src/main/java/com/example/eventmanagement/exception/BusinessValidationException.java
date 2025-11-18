package com.example.eventmanagement.exception;

public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) {
        super(message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessValidationException(Throwable cause) {
        super(cause);
    }
}
