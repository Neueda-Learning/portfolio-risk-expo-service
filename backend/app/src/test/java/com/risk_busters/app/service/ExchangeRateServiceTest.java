package com.risk_busters.app.service;

import com.risk_busters.app.dto.ExchangeRateDTO;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.model.Currency;
import com.risk_busters.app.model.ExchangeRate;
import com.risk_busters.app.repository.CurrencyRepository;
import com.risk_busters.app.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        service = new ExchangeRateService(exchangeRateRepository, currencyRepository);
    }

    @Test
    void getExchangeRatesByBaseCurrencyCode_throwsWhenCurrencyCodeDoesNotExist() {
        when(currencyRepository.existsByCurrencyCode("ZZZ")).thenReturn(false);

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.getExchangeRatesByBaseCurrencyCode("ZZZ"));
    }

    @Test
    void getExchangeRatesByBaseCurrencyCode_throwsWhenNoRatesForToday() {
        LocalDate today = LocalDate.now();
        when(currencyRepository.existsByCurrencyCode("USD")).thenReturn(true);
        when(exchangeRateRepository.findByFromCurrencyCurrencyCodeAndEffectiveDate("USD", today))
                .thenReturn(List.of());

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.getExchangeRatesByBaseCurrencyCode("USD"));
    }

    @Test
    void getExchangeRatesByBaseCurrencyCode_returnsRatesForToday() {
        LocalDate today = LocalDate.now();
        Currency usd = Currency.builder().currencyCode("USD").build();
        Currency eur = Currency.builder().currencyCode("EUR").build();

        ExchangeRate rate = ExchangeRate.builder()
                .fromCurrency(usd)
                .toCurrency(eur)
                .rate(new BigDecimal("0.920000"))
                .effectiveDate(today)
                .build();

        when(currencyRepository.existsByCurrencyCode("USD")).thenReturn(true);
        when(exchangeRateRepository.findByFromCurrencyCurrencyCodeAndEffectiveDate("USD", today))
                .thenReturn(List.of(rate));

        List<ExchangeRateDTO> result = service.getExchangeRatesByBaseCurrencyCode("USD");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(new BigDecimal("0.920000"), result.get(0).getRate());
        verify(exchangeRateRepository).findByFromCurrencyCurrencyCodeAndEffectiveDate("USD", today);
    }
}


