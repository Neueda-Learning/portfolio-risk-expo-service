package com.risk_busters.app.exceptions;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SnapshotPersistenceException extends RuntimeException {

    private final Integer portfolioId;
    private final LocalDate snapshotDate;

    public SnapshotPersistenceException(Integer portfolioId, LocalDate snapshotDate, String message, Throwable cause) {
        super(message, cause);
        this.portfolioId = portfolioId;
        this.snapshotDate = snapshotDate;
    }
}
