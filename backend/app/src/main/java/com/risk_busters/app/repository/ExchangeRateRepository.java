package com.risk_busters.app.repository;

import com.risk_busters.app.model.ExchangeRate;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Integer> {

    Optional<ExchangeRate> findByFromCurrencyCurrencyCodeAndToCurrencyCurrencyCodeAndEffectiveDate(
        String fromCurrency,
        String toCurrency,
        LocalDate effectiveDate
    );

    Optional<ExchangeRate> findTopByFromCurrencyCurrencyCodeAndToCurrencyCurrencyCodeAndIsActiveTrueAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
        String fromCurrency,
        String toCurrency,
        LocalDate asOfDate
    );

    List<ExchangeRate> findByFromCurrencyCurrencyCodeAndToCurrencyCurrencyCodeOrderByEffectiveDateDesc(
        String fromCurrency,
        String toCurrency
    );

    List<ExchangeRate> findByFromCurrencyCurrencyCodeAndEffectiveDate(String fromCurrencyCode, LocalDate effectiveDate);
    List<ExchangeRate> findAllByEffectiveDate(LocalDate effectiveDate);
}
