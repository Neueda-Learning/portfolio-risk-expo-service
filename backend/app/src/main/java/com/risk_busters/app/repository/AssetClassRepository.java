package com.risk_busters.app.repository;

import com.risk_busters.app.model.AssetClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetClassRepository extends JpaRepository<AssetClass, Integer> {
}

