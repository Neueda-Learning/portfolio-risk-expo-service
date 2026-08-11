package com.risk_busters.app.repository;

import com.risk_busters.app.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    Optional<PriceHistory> findTopByInstrumentInstrumentIdOrderByPriceDateDesc(Integer instrumentId);

    Optional<PriceHistory> findTopByInstrumentInstrumentIdAndPriceDateLessThanEqualOrderByPriceDateDesc(
            Integer instrumentId,
            LocalDate asOfDate
    );

    Optional<PriceHistory> findByInstrumentInstrumentIdAndPriceDate(Integer instrumentId, LocalDate priceDate);

    List<PriceHistory> findByInstrumentInstrumentIdOrderByPriceDateDesc(Integer instrumentId);
}


