package com.risk_busters.app.controller;

import com.risk_busters.app.exceptions.InsufficientPriceHistoryException;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.exceptions.SnapshotPersistenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnexpectedRollbackException.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedRollback(UnexpectedRollbackException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Transaction Conflict",
                "message", "The operation could not be completed because an internal sub-operation failed and caused the transaction to roll back.",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Bad Request",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(InsufficientPriceHistoryException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientPriceHistory(InsufficientPriceHistoryException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "error", "Insufficient Price History",
                "message", ex.getMessage(),
                "instrumentId", ex.getInstrumentId() != null ? ex.getInstrumentId() : "unknown",
                "availableDays", ex.getAvailableDays(),
                "requiredDays", ex.getRequiredDays(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(SnapshotPersistenceException.class)
    public ResponseEntity<Map<String, Object>> handleSnapshotPersistence(SnapshotPersistenceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Snapshot Persistence Failed",
                "message", ex.getMessage(),
                "portfolioId", ex.getPortfolioId() != null ? String.valueOf(ex.getPortfolioId()) : "unknown",
                "snapshotDate", ex.getSnapshotDate() != null ? ex.getSnapshotDate().toString() : "unknown",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "timestamp", Instant.now().toString()
        ));
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Not Found",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }
}
