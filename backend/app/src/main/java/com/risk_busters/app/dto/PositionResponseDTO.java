package com.risk_busters.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionResponseDTO {
    private Integer portfolioId;
    private Integer positionId;
    private Integer instrumentId;
    private String instrumentName;
    private BigDecimal quantity;
    private BigDecimal marketPrice;
    private BigDecimal marketValue;
    private BigDecimal marketValueBase;
    private BigDecimal weightPct;
    private BigDecimal costBasis;
    private LocalDate positionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
