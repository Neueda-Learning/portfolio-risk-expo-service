package com.risk_busters.app.service;

import com.risk_busters.app.dto.ExposureResponseDTO;
import com.risk_busters.app.exceptions.InsufficientPriceHistoryException;
import com.risk_busters.app.model.AssetClass;
import com.risk_busters.app.model.Instrument;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.model.PortfolioType;
import com.risk_busters.app.model.Position;
import com.risk_busters.app.model.PriceHistory;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import com.risk_busters.app.repository.PriceHistoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioRiskServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private PriceHistoryRepository priceHistoryRepository;
    @InjectMocks
    private PortfolioRiskService service;

    @Test
    void calculateExposure_aggregatesMultiplePositionsAcrossBaseAndNonBaseCurrencies() {
        Portfolio portfolio = portfolio("GBP");
        Position first = position(1, portfolio, instrument(1, "USD", "Technology"), new BigDecimal("100.00"));
        Position second = position(2, portfolio, instrument(2, "GBP", "Technology"), new BigDecimal("125.00"));
        Position third = position(3, portfolio, instrument(3, "EUR", "Healthcare"), new BigDecimal("240.00"));

        when(portfolioRepository.findById(1)).thenReturn(Optional.of(portfolio));
        when(positionRepository.findByPortfolioPortfolioId(1)).thenReturn(List.of(first, second, third));
        when(positionRepository.countByPortfolioId(1)).thenReturn(3);

        ExposureResponseDTO response = service.calculateExposure(1);

        Assertions.assertEquals(0, response.getTotalExposure().compareTo(new BigDecimal("465.00")));
        Assertions.assertEquals("GBP", response.getCurrency());
        Assertions.assertEquals(3, response.getPositionCount());
    }

    @Test
    void calculate1DayVar_usesExact252DaysOfPriceHistory() {
        Portfolio portfolio = portfolio("EUR");
        Position position = position(1, portfolio, instrument(1, "GBP", "Technology"), new BigDecimal("1000.00"));

        when(portfolioRepository.findById(1)).thenReturn(Optional.of(portfolio));
        when(positionRepository.findByPortfolioPortfolioId(1)).thenReturn(List.of(position));
        when(priceHistoryRepository.findByInstrumentInstrumentIdOrderByPriceDateDesc(1))
                .thenReturn(historicalPricesDescending(new BigDecimal("100.00")));

        Assertions.assertEquals(
                0,
                service.calculate1DayVar(1, 95).getVar1Day().compareTo(new BigDecimal("10.00"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("varEdgeCases")
    void calculate1DayVar_handlesKnownSeriesAndEdgeCases(String scenario,
                                                         String baseCurrency,
                                                         List<Position> positions,
                                                         List<PriceHistory> pricesDescending,
                                                         Class<? extends Throwable> expectedException,
                                                         BigDecimal expectedVar) {
        Portfolio portfolio = portfolio(baseCurrency);

        when(portfolioRepository.findById(1)).thenReturn(Optional.of(portfolio));
        when(positionRepository.findByPortfolioPortfolioId(1)).thenReturn(positions);
        lenient().when(priceHistoryRepository.findByInstrumentInstrumentIdOrderByPriceDateDesc(1))
                .thenReturn(pricesDescending);

        if (expectedException != null) {
            Assertions.assertThrows(expectedException, () -> service.calculate1DayVar(1, 95));
            return;
        }

        Assertions.assertEquals(0,
                service.calculate1DayVar(1, 95).getVar1Day().compareTo(expectedVar.setScale(2, RoundingMode.HALF_UP)));
    }

    private static Stream<Arguments> varEdgeCases() {
        Portfolio portfolio = portfolio("JPY");
        Position nonBaseCurrencyPosition = position(1, portfolio, instrument(1, "GBP", "Technology"), new BigDecimal("1000.00"));

        return Stream.of(
                Arguments.of(
                        "empty portfolio",
                        "USD",
                        List.<Position>of(),
                        List.<PriceHistory>of(),
                        IllegalArgumentException.class,
                        null
                ),
                Arguments.of(
                        "portfolio with no price data",
                        "GBP",
                        List.of(nonBaseCurrencyPosition),
                        List.<PriceHistory>of(),
                        InsufficientPriceHistoryException.class,
                        null
                ),
                Arguments.of(
                        "position in currency not matching base currency",
                        "EUR",
                        List.of(nonBaseCurrencyPosition),
                        historicalPricesDescending(new BigDecimal("100.00")),
                        null,
                        new BigDecimal("10.00")
                )
        );
    }

    private static Portfolio portfolio(String baseCurrency) {
        return Portfolio.builder()
                .portfolioId(1)
                .portfolioCode("PF-001")
                .portfolioName("Global Portfolio")
                .portfolioType(PortfolioType.MULTI_ASSET)
                .baseCurrency(baseCurrency)
                .aum(new BigDecimal("1000000.00"))
                .isActive(Boolean.TRUE)
                .build();
    }

    private static Position position(Integer positionId, Portfolio portfolio, Instrument instrument, BigDecimal marketValueBase) {
        return Position.builder()
                .positionId(positionId)
                .portfolio(portfolio)
                .instrument(instrument)
                .positionDate(LocalDate.of(2026, 3, 20))
                .quantity(new BigDecimal("10.00"))
                .marketPrice(new BigDecimal("10.00"))
                .marketValue(new BigDecimal("10.00"))
                .marketValueBase(marketValueBase)
                .weightPct(new BigDecimal("10.0000"))
                .build();
    }

    private static Instrument instrument(Integer instrumentId, String currency, String sector) {
        return Instrument.builder()
                .instrumentId(instrumentId)
                .instrumentIsin("US000000000" + instrumentId)
                .instrumentName("Instrument " + instrumentId)
                .assetClass(AssetClass.builder().assetClassId(1).assetClassName("Equities").isActive(Boolean.TRUE).build())
                .currency(currency)
                .sector(sector)
                .isActive(Boolean.TRUE)
                .build();
    }

    private static List<PriceHistory> historicalPricesDescending(BigDecimal startPrice) {
        List<PriceHistory> chronological = new ArrayList<>();
        BigDecimal price = startPrice;
        LocalDate date = LocalDate.of(2025, 1, 1);

        chronological.add(priceHistory(1, date, price));
        for (int i = 0; i < 13; i++) {
            date = date.plusDays(1);
            price = price.multiply(new BigDecimal("0.99")).setScale(4, RoundingMode.HALF_UP);
            chronological.add(priceHistory(chronological.size() + 1, date, price));
        }
        for (int i = 0; i < 238; i++) {
            date = date.plusDays(1);
            price = price.multiply(new BigDecimal("1.01")).setScale(4, RoundingMode.HALF_UP);
            chronological.add(priceHistory(chronological.size() + 1, date, price));
        }

        List<PriceHistory> descending = new ArrayList<>(chronological);
        java.util.Collections.reverse(descending);
        return descending;
    }

    private static PriceHistory priceHistory(Integer priceId, LocalDate date, BigDecimal closePrice) {
        return PriceHistory.builder()
                .priceId(priceId)
                .priceDate(date)
                .closePrice(closePrice)
                .currency("USD")
                .build();
    }
}
