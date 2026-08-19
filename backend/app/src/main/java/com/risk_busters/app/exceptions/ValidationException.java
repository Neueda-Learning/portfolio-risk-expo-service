package com.risk_busters.app.exceptions;

import lombok.Getter;

/**
 * Thrown when business logic validation fails.
 * Maps to HTTP 422 Unprocessable Entity.
 */
@Getter
public class ValidationException extends RuntimeException {
    private final String fieldName;
    private final Object rejectedValue;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.rejectedValue = null;
    }

    public ValidationException(String message, String fieldName, Object rejectedValue) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }
}
