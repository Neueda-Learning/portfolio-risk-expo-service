package com.risk_busters.app.repository;

import com.risk_busters.app.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    
    @Query("SELECT ph FROM PriceHistory ph LEFT JOIN FETCH ph.instrument WHERE ph.instrument.instrumentId = :instrumentId ORDER BY ph.priceDate DESC")
    List<PriceHistory> findByInstrumentInstrumentIdOrderByPriceDateDesc(@Param("instrumentId") Integer instrumentId);
}


