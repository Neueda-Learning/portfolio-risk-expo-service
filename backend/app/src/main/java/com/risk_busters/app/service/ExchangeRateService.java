package com.risk_busters.app.service;

import com.risk_busters.app.dto.ExchangeRateDTO;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.repository.CurrencyRepository;
import com.risk_busters.app.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    public List<ExchangeRateDTO> getAllExchangeRates(){
        log.info("Exchange rates retrieval started");
        List<ExchangeRateDTO> exchangeRates = exchangeRateRepository.findAll().stream()
                .map(exchangeRate -> ExchangeRateDTO.builder()
                        .fromCurrency(exchangeRate.getFromCurrency().getCurrencyCode())
                        .toCurrency(exchangeRate.getToCurrency().getCurrencyCode())
                        .rate(exchangeRate.getRate())
                        .build())
                .toList();
        log.info("Exchange rates retrieval completed. Found {} exchange rates", exchangeRates.size());
        return exchangeRates;
    }

    public List<ExchangeRateDTO> getExchangeRatesByBaseCurrencyCode(String fromCurrencyCode){
        log.info("Exchange rates retrieval started for base currency: {}", fromCurrencyCode);

        if (!currencyRepository.existsByCurrencyCode(fromCurrencyCode)) {
            throw new ResourceNotFoundException("Currency not found with code: " + fromCurrencyCode);
        }

        List<ExchangeRateDTO> exchangeRates = exchangeRateRepository
                .findByFromCurrencyCurrencyCodeAndEffectiveDate(fromCurrencyCode, LocalDate.now()).stream()
                .map(exchangeRate -> ExchangeRateDTO.builder()
                        .fromCurrency(exchangeRate.getFromCurrency().getCurrencyCode())
                        .toCurrency(exchangeRate.getToCurrency().getCurrencyCode())
                        .rate(exchangeRate.getRate())
                        .build())
                .toList();

        if (exchangeRates.isEmpty()) {
            throw new ResourceNotFoundException("No exchange rates found for base currency "
                    + fromCurrencyCode + " on date " + LocalDate.now());
        }

        log.info("Exchange rates retrieval completed for base currency: {}. Found {} exchange rates", fromCurrencyCode, exchangeRates.size());
        return exchangeRates;
    }
}

