package com.risk_busters.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "portfolio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer portfolioId;
    
    @Column(nullable = false)
    private String portfolioName;
    
    @Column(nullable = false)
    private String baseCurrency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "portfolio_type", length = 20, nullable = false)
    private PortfolioType portfolioType;
    
    private String managerName;
    
    @Column(nullable = false)
    private Boolean isActive;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Position> positions;
    
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Limit> limits;
}

