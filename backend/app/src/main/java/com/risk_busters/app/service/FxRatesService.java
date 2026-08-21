package com.risk_busters.app.service;

import com.risk_busters.app.clients.FrankfurterApiClient;
import com.risk_busters.app.dto.FrankfurterRatesDTO;
import com.risk_busters.app.model.Currency;
import com.risk_busters.app.model.ExchangeRate;
import com.risk_busters.app.repository.CurrencyRepository;
import com.risk_busters.app.repository.ExchangeRateRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@EnableScheduling
public class FxRatesService {
    private static final Logger log = LoggerFactory.getLogger(FxRatesService.class);

    private final FrankfurterApiClient frankfurterClient;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public FxRatesService(FrankfurterApiClient frankfurterClient,
                         CurrencyRepository currencyRepository,
                         ExchangeRateRepository exchangeRateRepository) {
        this.frankfurterClient = frankfurterClient;
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }
    @Scheduled(cron = "0 15 * * * ?")
    @Scheduled(fixedDelay = 30000) //TEST
    @Transactional
    public void synchronizeExchangeRates() {
        log.info("Starting daily FX rate synchronization...");

        // 1. Fetch all active currencies from the LAYER 1 table
        List<Currency> activeCurrencies = currencyRepository.findByIsActiveTrue();
        List<String> activeCodes = activeCurrencies.stream()
                .map(Currency::getCurrencyCode)
                .toList();

        if (activeCodes.size() < 2) {
            log.warn("Not enough active currencies in the database to form FX pairs.");
            return;
        }

        LocalDate today = LocalDate.now();
        
        // 2. Idempotency check: if rates for today already exist, skip synchronization
        long existingRatesForToday = exchangeRateRepository
                .findByFromCurrencyCurrencyCodeAndEffectiveDate(activeCodes.get(0), today)
                .size();
        
        int expectedRateCount = activeCodes.size() * (activeCodes.size() - 1);
        if (existingRatesForToday > 0 && existingRatesForToday == expectedRateCount) {
            log.info("FX rate synchronization already completed for {}. Skipping.", today);
            return;
        }

        List<ExchangeRate> ratesToSave = new ArrayList<>();

        // 3. Iterate through each active currency to act as the base
        for (String baseCode : activeCodes) {

            // Filter out the base currency from the targets list
            List<String> targetCodes = activeCodes.stream()
                    .filter(code -> !code.equals(baseCode))
                    .toList();

            // 4. Call external API for specific pairs
            List<FrankfurterRatesDTO> responses = frankfurterClient.getRatesForBase(baseCode, targetCodes);

            // 5. Map DTOs to JPA Entities, checking for existence per pair (insert-only approach)
            for (FrankfurterRatesDTO response : responses) {
                var existingRate = exchangeRateRepository
                        .findByFromCurrencyCurrencyCodeAndToCurrencyCurrencyCodeAndEffectiveDate(
                                baseCode,
                                response.getQuote(),
                                response.getDate()
                        );

                if (existingRate.isEmpty()) {
                    ExchangeRate entity = new ExchangeRate();

                    entity.setFromCurrency(getCurrencyReference(activeCurrencies, baseCode));
                    entity.setToCurrency(getCurrencyReference(activeCurrencies, response.getQuote()));

                    entity.setRate(response.getRate());
                    entity.setEffectiveDate(response.getDate());
                    entity.setSource("FRANKFURTER");
                    entity.setIsActive(true);

                    ratesToSave.add(entity);
                    log.debug("Inserted: new rate for {}/{} on {}.", baseCode, response.getQuote(), response.getDate());
                } else {
                    log.debug("Skipped: rate for {}/{} on {} already exists.",
                            baseCode, response.getQuote(), today);
                }
            }
        }

        // 6. Batch save the new records
        if (!ratesToSave.isEmpty()) {
            exchangeRateRepository.saveAll(ratesToSave);
            log.info("Successfully synchronized {} new exchange rates.", ratesToSave.size());
        } else {
            log.info("FX rate synchronization completed. No new rates to insert.");
        }
    }

    /**
     * Helper to retrieve the Currency object reference without hitting the DB again.
     */
    private Currency getCurrencyReference(List<Currency> activeCurrencies, String targetCode) {
        return activeCurrencies.stream()
                .filter(c -> c.getCurrencyCode().equals(targetCode))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Currency code not found: " + targetCode));
    }
}

