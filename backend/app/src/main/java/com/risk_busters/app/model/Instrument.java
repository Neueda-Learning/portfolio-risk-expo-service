package com.risk_busters.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "instrument")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instrument {
    
    @Id
    @Column(name = "instrument_id")
    private Integer instrumentId;
    
    @Column(name = "instrument_isin", nullable = false, unique = true, length = 12)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String instrumentIsin;

    @Column(name = "instrument_name", nullable = false, length = 100)
    private String instrumentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_class_id", nullable = false)
    private com.risk_busters.app.model.AssetClass assetClass;

    @Column(nullable = false, length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(length = 100)
    private String issuer;

    @Column(length = 50)
    private String sector;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PriceHistory> priceHistory;
    
    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Position> positions;
}

