package com.risk_busters.app.repository;

import com.risk_busters.app.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Integer> {
    
    @Query("SELECT p FROM Position p LEFT JOIN FETCH p.instrument LEFT JOIN FETCH p.portfolio WHERE p.portfolio.portfolioId = :portfolioId")
    List<Position> findByPortfolioPortfolioId(@Param("portfolioId") Integer portfolioId);
    
    @Query("SELECT COUNT(p) FROM Position p WHERE p.portfolio.portfolioId = :portfolioId")
    Integer countByPortfolioId(@Param("portfolioId") Integer portfolioId);
}

