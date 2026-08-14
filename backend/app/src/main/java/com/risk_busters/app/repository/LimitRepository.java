package com.risk_busters.app.repository;

import com.risk_busters.app.dto.LimitDetailDTO;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimitRepository extends JpaRepository<Limit, Integer> {
    List<Limit> findByStatus(LimitStatus status);

    List<Limit> findByPortfolioPortfolioId(Integer portfolioId);

    List<Limit> findByPortfolioPortfolioIdAndStatus(Integer portfolioId, LimitStatus status);

    List<Limit> findByPortfolioPortfolioIdAndStatusIn(Integer portfolioId, List<LimitStatus> statuses);
}

