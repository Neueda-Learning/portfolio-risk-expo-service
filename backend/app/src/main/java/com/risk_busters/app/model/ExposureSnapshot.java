package com.risk_busters.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exposure_snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExposureSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Integer snapshotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_exposure", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalExposure;

    @Column(name = "var_1day_95", precision = 15, scale = 2)
    private BigDecimal var1Day95;

    @Column(name = "var_1day_99", precision = 15, scale = 2)
    private BigDecimal var1Day99;

    @Column(name = "var_10day_99", precision = 15, scale = 2)
    private BigDecimal var10Day99;

    @Column(name = "largest_position_pct", precision = 8, scale = 4)
    private BigDecimal largestPositionPct;

    @Column(name = "currency", nullable = false, length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;

    @Column(name = "num_positions")
    private Integer numPositions;

    @Column(name = "concentration_herfindahl", precision = 8, scale = 4)
    private BigDecimal concentrationHerfindahl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

