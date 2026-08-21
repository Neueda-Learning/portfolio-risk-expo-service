package com.risk_busters.app.repository;

import com.risk_busters.app.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    List<Currency> findByIsActiveTrue();

    boolean existsByCurrencyCode(String currencyCode);
}

