package com.risk_busters.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioLimitsResponseDTO {
    private Integer portfolioId;
    private String portfolioName;
    private BigDecimal totalExposure;
    private String baseCurrency;
    private List<LimitDetailDTO> limits;
}

