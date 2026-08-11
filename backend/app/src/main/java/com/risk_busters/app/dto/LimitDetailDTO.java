package com.risk_busters.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitDetailDTO {
    private Integer limitId;
    private String limitType;
    private String limitMetric;
    private BigDecimal limitValue;
    private BigDecimal warningThreshold;
    private BigDecimal currentValue;
    private BigDecimal utilisationPct;
    private String status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isBreached;
}

