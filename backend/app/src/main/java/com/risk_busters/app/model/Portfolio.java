package com.risk_busters.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "portfolio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {
    
    @Id
    @Column(name = "portfolio_id")
    private Integer portfolioId;

    @Column(name = "portfolio_code", nullable = false, unique = true, length = 20)
    private String portfolioCode;
    
    @Column(name = "portfolio_name", nullable = false, length = 100)
    private String portfolioName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "portfolio_type", length = 20, nullable = false)
    private PortfolioType portfolioType;

    @Column(name = "base_currency", nullable = false, length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String baseCurrency;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal aum;

    @Column(length = 50)
    private String benchmark;

    @Column(name = "risk_mandate", length = 200)
    private String riskMandate;
    
    @Column(name = "manager", length = 60)
    private String manager;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Position> positions;
    
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Limit> limits;
}

