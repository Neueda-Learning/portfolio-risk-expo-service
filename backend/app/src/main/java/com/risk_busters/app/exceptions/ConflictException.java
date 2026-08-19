package com.risk_busters.app.exceptions;

/**
 * Thrown when an operation conflicts with existing data or resource state.
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
