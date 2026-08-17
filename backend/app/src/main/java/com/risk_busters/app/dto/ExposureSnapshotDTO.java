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
public class ExposureSnapshotDTO {
    private Integer snapshotId;
    private LocalDate snapshotDate;
    private BigDecimal totalExposure;
    private BigDecimal var1Day95;
    private BigDecimal var1Day99;
    private BigDecimal var10Day99;
    private BigDecimal largestPositionPct;
    private String currency;
    private Integer numPositions;
    private BigDecimal concentrationHerfindahl;
}
