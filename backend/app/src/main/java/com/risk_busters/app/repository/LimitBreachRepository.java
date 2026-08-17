package com.risk_busters.app.repository;

import com.risk_busters.app.model.LimitBreach;
import com.risk_busters.app.model.LimitBreachStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LimitBreachRepository extends JpaRepository<LimitBreach, Integer> {
    List<LimitBreach> findByStatus(LimitBreachStatus status);

    List<LimitBreach> findByLimitLimitIdAndStatus(Integer limitId, LimitBreachStatus status);

    Optional<LimitBreach> findFirstByLimitLimitIdOrderByBreachDateDesc(Integer limitId);

    /**
     * Used by {@code LimitBreachPersistenceService} to generate the next breach ID
     * when the underlying table has no database sequence.
     */
    @Query("SELECT COALESCE(MAX(lb.breachId), 0) FROM LimitBreach lb")
    Integer findMaxBreachId();

    /**
     * Idempotency guard — prevents inserting a duplicate breach for the same
     * limit on the same date within a single day's check run.
     */
    boolean existsByLimitLimitIdAndBreachDate(Integer limitId, LocalDate breachDate);
}
