package com.risk_busters.app.exceptions;

import lombok.Getter;

/**
 * Thrown when there is not enough price history available to perform a
 * VaR (or similar) calculation for a given instrument or portfolio.
 */
@Getter
public class InsufficientPriceHistoryException extends RuntimeException {

    private final Integer instrumentId;
    private final int availableDays;
    private final int requiredDays;

    public InsufficientPriceHistoryException(Integer instrumentId, int availableDays, int requiredDays) {
        super("Insufficient price history for instrument " + instrumentId
                + ": " + availableDays + " day(s) available, " + requiredDays + " required.");
        this.instrumentId = instrumentId;
        this.availableDays = availableDays;
        this.requiredDays = requiredDays;
    }
}


