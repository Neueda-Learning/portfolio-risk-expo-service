package com.risk_busters.app.repository;

import com.risk_busters.app.model.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, Integer> {

    @Query("""
            SELECT i
            FROM Instrument i
            JOIN i.positions p
            LEFT JOIN FETCH i.assetClass
            WHERE p.positionId = :positionId
              AND p.portfolio.portfolioId = :portfolioId
            """)
    Optional<Instrument> findByPositionIdAndPortfolioId(@Param("portfolioId") Integer portfolioId,
                                                        @Param("positionId") Integer positionId);
}
