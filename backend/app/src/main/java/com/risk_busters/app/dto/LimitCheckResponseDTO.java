package com.risk_busters.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LimitCheckResponseDTO {

    private Integer portfolioId;
    private String portfolioName;
    private LocalDateTime checkedAt;

    /** Number of limits evaluated (including skipped). */
    private int totalLimits;

    /** Number of limits where actualValue > limitValue. */
    private int breachedCount;

    /** Number of limits where actualValue > warningThreshold (but not breached). */
    private int warningCount;

    /** Number of limits that could not be evaluated. */
    private int skippedCount;

    /** Number of new LimitBreach records written to the database in this run. */
    private int newBreachesRecorded;

    private List<LimitCheckResultDTO> results;
}

