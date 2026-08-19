package com.risk_busters.app.repository;

import com.risk_busters.app.model.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimitRepository extends JpaRepository<Limit, Integer> {
    
    @Query("SELECT l FROM Limit l LEFT JOIN FETCH l.portfolio WHERE l.portfolio.portfolioId = :portfolioId")
    List<Limit> findByPortfolioPortfolioId(@Param("portfolioId") Integer portfolioId);
}

