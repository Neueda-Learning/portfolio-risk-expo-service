package com.risk_busters.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExposureResponseDTO {
    private Integer portfolioId;
    private String portfolioName;
    private BigDecimal totalExposure;
    private String currency;
    private Integer positionCount;
}

