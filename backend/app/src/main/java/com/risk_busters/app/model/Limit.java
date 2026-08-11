package com.risk_busters.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "limit_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Limit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer limitId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @Column(nullable = false)
    private String limitType;
    
    @Column(nullable = false)
    private BigDecimal limitValue;
    
    private BigDecimal warningPct;
    
    @Column(nullable = false)
    private String currency;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

