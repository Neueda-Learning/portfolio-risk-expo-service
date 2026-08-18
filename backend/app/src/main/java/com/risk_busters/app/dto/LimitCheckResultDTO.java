package com.risk_busters.app.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Holds the result of comparing a single risk limit against its current computed metric value.
 * Produced by LimitComparisonService — no side effects, pure calculation output.
 */
@Data
@Builder
public class LimitCheckResultDTO {

    private Integer limitId;
    private Integer portfolioId;

    /** LimitType enum name (VAR, CONCENTRATION, SECTOR_EXPOSURE, LEVERAGE, DURATION, DRAWDOWN). */
    private String limitType;

    /** Optional qualifier stored on the limit (e.g. "VAR_99", sector name, …). */
    private String limitMetric;

    private BigDecimal limitValue;
    private BigDecimal warningThreshold;

    /** Freshly calculated current value for the metric. Null when skipped. */
    private BigDecimal actualValue;

    /** actualValue / limitValue * 100, rounded to 4 dp. Null when skipped. */
    private BigDecimal utilisationPct;

    /** True when actualValue > limitValue. */
    private boolean breached;

    /** True when actualValue > warningThreshold but not yet breached. */
    private boolean warning;

    /** actualValue - limitValue when breached, otherwise null. */
    private BigDecimal excessAmount;

    /** MINOR / MAJOR / CRITICAL — populated only when breached. */
    private String severity;

    /** True when the metric could not be computed (insufficient data, unsupported type, …). */
    private boolean skipped;

    private String skipReason;
}

