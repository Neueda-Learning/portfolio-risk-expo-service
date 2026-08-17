package com.risk_busters.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "limit_breach")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitBreach {

    @Id
    @Column(name = "breach_id")
    private Integer breachId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "limit_id", nullable = false)
    private Limit limit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "breach_date", nullable = false)
    private LocalDate breachDate;

    @Column(name = "limit_value", precision = 15, scale = 4)
    private BigDecimal limitValue;

    @Column(name = "actual_value", precision = 15, scale = 4)
    private BigDecimal actualValue;

    @Column(name = "excess_amount", precision = 15, scale = 4)
    private BigDecimal excessAmount;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "acknowledged_by", length = 60)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolution", length = 300)
    private String resolution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LimitBreachStatus status;
}

