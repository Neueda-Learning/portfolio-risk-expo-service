package com.risk_busters.app.repository;

import com.risk_busters.app.model.ExposureSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExposureSnapshotRepository extends JpaRepository<ExposureSnapshot, Integer> {
    List<ExposureSnapshot> findByPortfolioPortfolioIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            Integer portfolioId,
            LocalDate startDate,
            LocalDate endDate
    );
}
