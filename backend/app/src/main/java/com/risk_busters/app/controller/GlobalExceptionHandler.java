package com.risk_busters.app.controller;

import com.risk_busters.app.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(
                "Not Found",
                ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildErrorResponse(
                "Bad Request",
                ex.getMessage()
        ));
    }

    @ExceptionHandler(InsufficientPriceHistoryException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientPriceHistory(InsufficientPriceHistoryException ex) {
        log.warn("Insufficient price history for instrument: {}", ex.getInstrumentId());
        Map<String, Object> response = buildErrorResponse(
                "Insufficient Price History",
                ex.getMessage()
        );
        response.put("instrumentId", ex.getInstrumentId() != null ? ex.getInstrumentId() : "unknown");
        response.put("availableDays", ex.getAvailableDays());
        response.put("requiredDays", ex.getRequiredDays());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(SnapshotPersistenceException.class)
    public ResponseEntity<Map<String, Object>> handleSnapshotPersistence(SnapshotPersistenceException ex) {
        log.error("Snapshot persistence failed for portfolio: {}", ex.getPortfolioId(), ex);
        Map<String, Object> response = buildErrorResponse(
                "Snapshot Persistence Failed",
                ex.getMessage()
        );
        response.put("portfolioId", ex.getPortfolioId() != null ? ex.getPortfolioId() : "unknown");
        response.put("snapshotDate", ex.getSnapshotDate() != null ? ex.getSnapshotDate().toString() : "unknown");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UnexpectedRollbackException.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedRollback(UnexpectedRollbackException ex) {
        log.error("Transaction rollback occurred", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(buildErrorResponse(
                "Transaction Conflict",
                "The operation could not be completed because an internal sub-operation failed and caused the transaction to roll back."
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse(
                "Internal Server Error",
                "An unexpected error occurred. Please contact support."
        ));
    }

    private Map<String, Object> buildErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        response.put("timestamp", Instant.now().toString());
        return response;
    }
}
