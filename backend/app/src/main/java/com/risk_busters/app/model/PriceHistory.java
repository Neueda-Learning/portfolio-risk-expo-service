package com.risk_busters.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer priceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;
    
    @Column(nullable = false)
    private LocalDate priceDate;
    
    @Column(nullable = false)
    private BigDecimal closePrice;
    
    @Column(nullable = false)
    private String currency;
    
    private String source;
}

