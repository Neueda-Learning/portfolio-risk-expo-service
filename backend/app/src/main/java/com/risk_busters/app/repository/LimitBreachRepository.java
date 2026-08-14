package com.risk_busters.app.repository;

import com.risk_busters.app.model.LimitBreach;
import com.risk_busters.app.model.LimitBreachStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LimitBreachRepository extends JpaRepository<LimitBreach, Integer> {
    List<LimitBreach> findByStatus(LimitBreachStatus status);

    List<LimitBreach> findByLimitLimitIdAndStatus(Integer limitId, LimitBreachStatus status);

    Optional<LimitBreach> findFirstByLimitLimitIdOrderByBreachDateDesc(Integer limitId);
}
