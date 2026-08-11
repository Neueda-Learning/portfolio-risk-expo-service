package com.risk_busters.app.repository;

import com.risk_busters.app.model.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimitRepository extends JpaRepository<Limit, Integer> {
    List<Limit> findByPortfolioPortfolioId(Integer portfolioId);
}

