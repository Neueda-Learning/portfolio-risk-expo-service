package com.risk_busters.app.dto;

import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitBreachStatus;
import com.risk_busters.app.model.Portfolio;
import jakarta.persistence.*;
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
public class LimitBreachDTO {
    private Integer breachId;
    private Integer limitId;
    private Integer portfolioId;
    private String portfolioName;
    private LocalDate breachDate;
    private BigDecimal limitValue;
    private BigDecimal actualValue;
    private BigDecimal excessAmount;
    private String severity;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private String resolution;
    private LimitBreachStatus status;
    //LimitType ???
}
