package com.risk_busters.app.repository;

import com.risk_busters.app.model.LimitBreach;
import com.risk_busters.app.model.LimitBreachStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LimitBreachRepository extends JpaRepository<LimitBreach, Integer> {
    
    @Query("SELECT lb FROM LimitBreach lb LEFT JOIN FETCH lb.limit LEFT JOIN FETCH lb.portfolio WHERE lb.status = :status")
    List<LimitBreach> findByStatus(@Param("status") LimitBreachStatus status);

    @Query("SELECT lb FROM LimitBreach lb LEFT JOIN FETCH lb.limit LEFT JOIN FETCH lb.portfolio WHERE lb.limit.limitId = :limitId ORDER BY lb.breachDate DESC")
    Optional<LimitBreach> findFirstByLimitLimitIdOrderByBreachDateDesc(@Param("limitId") Integer limitId);

    @Query("SELECT COALESCE(MAX(lb.breachId), 0) FROM LimitBreach lb")
    Integer findMaxBreachId();

    boolean existsByLimitLimitIdAndBreachDate(Integer limitId, LocalDate breachDate);
}
