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
public class LimitDetailDTO {
    private Integer limitId;
    private String limitType;
    private BigDecimal limitValue;
    private BigDecimal currentUtilisation;
    private BigDecimal utilisationPct;
    private BigDecimal warningPct;
    private Boolean isBreached;
    private String currency;
}

