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
@Table(name = "price_history", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"instrument_id", "price_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {
    
    @Id
    @Column(name = "price_id")
    private Integer priceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;
    
    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;
    
    @Column(name = "close_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal closePrice;

    @Column(name = "open_price", precision = 15, scale = 4)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 15, scale = 4)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 15, scale = 4)
    private BigDecimal lowPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal volume;
    
    @Column(nullable = false, length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;
    
    @Column(length = 50)
    private String source;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

