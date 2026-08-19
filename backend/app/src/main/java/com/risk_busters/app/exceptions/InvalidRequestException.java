package com.risk_busters.app.exceptions;

/**
 * Thrown when an API request has invalid parameters or malformed data.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
