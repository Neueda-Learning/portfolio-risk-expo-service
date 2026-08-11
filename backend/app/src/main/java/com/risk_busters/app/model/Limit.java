package com.risk_busters.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "risk_limit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Limit {
    
    @Id
    @Column(name = "limit_id")
    private Integer limitId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", nullable = false, length = 30)
    private com.risk_busters.app.model.LimitType limitType;

    @Column(name = "limit_metric", length = 50)
    private String limitMetric;
    
    @Column(name = "limit_value", precision = 15, scale = 4)
    private BigDecimal limitValue;
    
    @Column(name = "warning_threshold", precision = 15, scale = 4)
    private BigDecimal warningThreshold;
    
    @Column(name = "current_value", precision = 15, scale = 4)
    private BigDecimal currentValue;

    @Column(name = "utilisation_pct", precision = 8, scale = 4)
    private BigDecimal utilisationPct;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private com.risk_busters.app.model.LimitStatus status;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}

