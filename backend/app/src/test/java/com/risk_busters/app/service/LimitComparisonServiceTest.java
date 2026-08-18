package com.risk_busters.app.service;

import com.risk_busters.app.dto.LimitCheckResultDTO;
import com.risk_busters.app.model.AssetClass;
import com.risk_busters.app.model.Instrument;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.model.LimitType;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.model.PortfolioType;
import com.risk_busters.app.model.Position;
import com.risk_busters.app.repository.LimitRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimitComparisonServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private LimitRepository limitRepository;
    @Mock
    private PortfolioRiskService portfolioRiskService;

    @InjectMocks
    private LimitComparisonService service;

    @ParameterizedTest(name = "sector concentration {0}%")
    @MethodSource("sectorConcentrationScenarios")
    void compareAllLimitsInPortfolio_flagsSectorConcentrationAbove25Percent(BigDecimal sectorExposure,
                                                                            boolean expectedBreached) {
        Portfolio portfolio = portfolio();
        Limit limit = Limit.builder()
                .limitId(1)
                .portfolio(portfolio)
                .limitType(LimitType.SECTOR_CONC)
                .limitMetric("Technology")
                .limitValue(new BigDecimal("25.00"))
                .warningThreshold(new BigDecimal("20.00"))
                .status(LimitStatus.OK)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(portfolioRepository.findById(1)).thenReturn(Optional.of(portfolio));
        when(limitRepository.findByPortfolioPortfolioId(1)).thenReturn(List.of(limit));
        when(positionRepository.findByPortfolioPortfolioId(1)).thenReturn(List.of(
                position(1, portfolio, instrument(1, "USD", "Technology", "Equities"), sectorExposure, sectorExposure),
                position(2, portfolio, instrument(2, "USD", "Healthcare", "Equities"), new BigDecimal("100.00").subtract(sectorExposure), new BigDecimal("100.00").subtract(sectorExposure))
        ));

        LimitCheckResultDTO result = service.compareAllLimitsInPortfolio(1).get(0);

        Assertions.assertEquals(expectedBreached, result.isBreached());
        Assertions.assertEquals(0, result.getActualValue().compareTo(sectorExposure));
        Assertions.assertEquals(new BigDecimal("25.00"), result.getLimitValue());
        Assertions.assertEquals(expectedBreached ? "MAJOR" : null, result.getSeverity());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = LimitType.class, names = {"TOTAL_EXPOSURE", "SECTOR_CONC", "ASSET_CLASS_CONC", "SINGLE_NAME"})
    void compareAllLimitsInPortfolio_evaluatesEachRequestedLimitTypeIndependently(LimitType limitType) {
        Portfolio portfolio = portfolio();
        Limit limit = buildLimitForType(limitType, portfolio);
        List<Position> positions = positionsForType(limitType, portfolio);
        BigDecimal expectedActual = expectedActualForType(limitType);

        when(portfolioRepository.findById(1)).thenReturn(Optional.of(portfolio));
        when(limitRepository.findByPortfolioPortfolioId(1)).thenReturn(List.of(limit));
        when(positionRepository.findByPortfolioPortfolioId(1)).thenReturn(positions);

        LimitCheckResultDTO result = service.compareAllLimitsInPortfolio(1).get(0);

        Assertions.assertEquals(limitType.name(), result.getLimitType());
        Assertions.assertTrue(result.isBreached());
        Assertions.assertEquals(0, result.getActualValue().compareTo(expectedActual));
    }

    private static Stream<Arguments> sectorConcentrationScenarios() {
        return Stream.of(
                Arguments.of(new BigDecimal("30.00"), true),
                Arguments.of(new BigDecimal("24.90"), false)
        );
    }

    private static Portfolio portfolio() {
        return Portfolio.builder()
                .portfolioId(1)
                .portfolioCode("PF-001")
                .portfolioName("Global Portfolio")
                .portfolioType(PortfolioType.MULTI_ASSET)
                .baseCurrency("USD")
                .aum(new BigDecimal("1000000.00"))
                .isActive(Boolean.TRUE)
                .build();
    }

    private static Limit buildLimitForType(LimitType limitType, Portfolio portfolio) {
        return Limit.builder()
                .limitId(10)
                .portfolio(portfolio)
                .limitType(limitType)
                .limitMetric(switch (limitType) {
                    case TOTAL_EXPOSURE -> "TOTAL_EXPOSURE";
                    case SECTOR_CONC -> "Technology";
                    case ASSET_CLASS_CONC -> "Equities";
                    case SINGLE_NAME -> "SINGLE_NAME";
                    default -> "VAR";
                })
                .limitValue(switch (limitType) {
                    case TOTAL_EXPOSURE -> new BigDecimal("100.00");
                    case SECTOR_CONC, ASSET_CLASS_CONC, SINGLE_NAME -> new BigDecimal("25.00");
                    default -> new BigDecimal("100.00");
                })
                .warningThreshold(new BigDecimal("90.00"))
                .status(LimitStatus.OK)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();
    }

    private static BigDecimal expectedActualForType(LimitType limitType) {
        return switch (limitType) {
            case TOTAL_EXPOSURE -> new BigDecimal("110.00");
            case SECTOR_CONC -> new BigDecimal("30.00");
            case ASSET_CLASS_CONC -> new BigDecimal("30.00");
            case SINGLE_NAME -> new BigDecimal("70.00");
            default -> BigDecimal.ZERO;
        };
    }

    private static List<Position> positionsForType(LimitType limitType, Portfolio portfolio) {
        return switch (limitType) {
            case TOTAL_EXPOSURE -> List.of(
                    position(1, portfolio, instrument(1, "USD", "Technology", "Equities"), new BigDecimal("60.00"), new BigDecimal("60.00")),
                    position(2, portfolio, instrument(2, "USD", "Healthcare", "Equities"), new BigDecimal("50.00"), new BigDecimal("50.00"))
            );
            case SECTOR_CONC -> List.of(
                    position(1, portfolio, instrument(1, "USD", "Technology", "Equities"), new BigDecimal("30.00"), new BigDecimal("30.00")),
                    position(2, portfolio, instrument(2, "USD", "Healthcare", "Equities"), new BigDecimal("70.00"), new BigDecimal("70.00"))
            );
            case ASSET_CLASS_CONC -> List.of(
                    position(1, portfolio, instrument(1, "USD", "Technology", "Equities"), new BigDecimal("30.00"), new BigDecimal("30.00")),
                    position(2, portfolio, instrument(2, "USD", "Healthcare", "Fixed Income"), new BigDecimal("70.00"), new BigDecimal("70.00"))
            );
            case SINGLE_NAME -> List.of(
                    position(1, portfolio, instrument(1, "USD", "Technology", "Equities"), new BigDecimal("100.00"), new BigDecimal("70.00")),
                    position(2, portfolio, instrument(2, "USD", "Healthcare", "Equities"), new BigDecimal("100.00"), new BigDecimal("30.00"))
            );
            default -> List.of();
        };
    }

    private static Position position(Integer positionId,
                                     Portfolio portfolio,
                                     Instrument instrument,
                                     BigDecimal marketValueBase,
                                     BigDecimal weightPct) {
        return Position.builder()
                .positionId(positionId)
                .portfolio(portfolio)
                .instrument(instrument)
                .positionDate(LocalDate.of(2026, 3, 20))
                .quantity(new BigDecimal("10.00"))
                .marketPrice(new BigDecimal("10.00"))
                .marketValue(marketValueBase)
                .marketValueBase(marketValueBase)
                .weightPct(weightPct)
                .build();
    }

    private static Instrument instrument(Integer instrumentId, String currency, String sector, String assetClassName) {
        return Instrument.builder()
                .instrumentId(instrumentId)
                .instrumentIsin("US000000000" + instrumentId)
                .instrumentName("Instrument " + instrumentId)
                .currency(currency)
                .sector(sector)
                .assetClass(AssetClass.builder()
                        .assetClassId(instrumentId)
                        .assetClassCode(assetClassName.substring(0, Math.min(10, assetClassName.length())).toUpperCase())
                        .assetClassName(assetClassName)
                        .isActive(Boolean.TRUE)
                        .build())
                .isActive(Boolean.TRUE)
                .build();
    }
}
