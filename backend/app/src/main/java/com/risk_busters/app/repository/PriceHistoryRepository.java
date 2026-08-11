package com.risk_busters.app.repository;

import com.risk_busters.app.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    @Query("SELECT p FROM PriceHistory p WHERE p.instrument.instrumentId = :instrumentId ORDER BY p.priceDate DESC LIMIT 1")
    Optional<PriceHistory> findLatestPriceByInstrumentId(@Param("instrumentId") Integer instrumentId);
    
    @Query("SELECT p FROM PriceHistory p WHERE p.instrument.instrumentId = :instrumentId AND p.priceDate <= :asOfDate ORDER BY p.priceDate DESC LIMIT 1")
    Optional<PriceHistory> findPriceByInstrumentIdAsOfDate(@Param("instrumentId") Integer instrumentId, @Param("asOfDate") LocalDate asOfDate);
    
    List<PriceHistory> findByInstrumentInstrumentIdOrderByPriceDateDesc(Integer instrumentId);
}


