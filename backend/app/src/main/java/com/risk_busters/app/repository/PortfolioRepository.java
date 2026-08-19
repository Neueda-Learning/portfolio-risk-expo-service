package com.risk_busters.app.repository;

import com.risk_busters.app.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {
    
    @Modifying
    @Query(value = "CALL store_snapshot(:portfolioId, :snapshotDate)", nativeQuery = true)
    void storeSnapshot(@Param("portfolioId") Integer portfolioId,
                       @Param("snapshotDate") LocalDate snapshotDate);
}
