package com.risk_busters.app.dto;

import com.risk_busters.app.model.PortfolioType;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfoliosDTO {
    private Integer portfolioId;
    private String portfolioCode;
    private String portfolioName;
    private PortfolioType portfolioType;
    private String baseCurrency;
    private BigDecimal aum;
    private String benchmark;
    private String riskMandate;
    private String manager;
    private Boolean isActive;
}
