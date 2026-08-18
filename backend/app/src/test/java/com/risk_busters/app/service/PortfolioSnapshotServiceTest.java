package com.risk_busters.app.service;

import com.risk_busters.app.dto.ExposureSnapshotDTO;
import com.risk_busters.app.dto.GetSnapshotResponseDTO;
import com.risk_busters.app.model.ExposureSnapshot;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.model.PortfolioType;
import com.risk_busters.app.repository.ExposureSnapshotRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioSnapshotServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private ExposureSnapshotRepository exposureSnapshotRepository;

    @Captor
    private ArgumentCaptor<Integer> portfolioIdCaptor;

    @Captor
    private ArgumentCaptor<LocalDate> snapshotDateCaptor;

    @Test
    void snapshotsEndOfDayProcedureCreatesSnapshotsForAllPortfolios() {
        PortfolioSnapshotService portfolioSnapshotService = new PortfolioSnapshotService(
                portfolioRepository,
                exposureSnapshotRepository);

        Portfolio firstPortfolio = Portfolio.builder()
                .portfolioId(1)
                .portfolioCode("PF-001")
                .portfolioName("Alpha Growth")
                .portfolioType(PortfolioType.EQUITY)
                .baseCurrency("USD")
                .aum(new BigDecimal("1000000.00"))
                .isActive(true)
                .build();

        Portfolio secondPortfolio = Portfolio.builder()
                .portfolioId(2)
                .portfolioCode("PF-002")
                .portfolioName("Balanced Income")
                .portfolioType(PortfolioType.MULTI_ASSET)
                .baseCurrency("GBP")
                .aum(new BigDecimal("2500000.00"))
                .isActive(true)
                .build();

        when(portfolioRepository.findAll()).thenReturn(List.of(firstPortfolio, secondPortfolio));

        portfolioSnapshotService.snapshotsEndOfDayProcedure();

        verify(portfolioRepository).findAll();
        verify(portfolioRepository, times(2)).storeSnapshot(portfolioIdCaptor.capture(), snapshotDateCaptor.capture());
        verifyNoMoreInteractions(portfolioRepository);

        assertEquals(List.of(1, 2), portfolioIdCaptor.getAllValues());

        LocalDate expectedSnapshotDate = LocalDate.now(ZoneId.of("Europe/London"));
        assertEquals(List.of(expectedSnapshotDate, expectedSnapshotDate), snapshotDateCaptor.getAllValues());
    }

    @Test
    void getPortfolioSnapshots_mapsAllSnapshotFields() {
        PortfolioSnapshotService portfolioSnapshotService = new PortfolioSnapshotService(
                portfolioRepository,
                exposureSnapshotRepository);

        Portfolio portfolio = Portfolio.builder()
                .portfolioId(1)
                .portfolioCode("PF-001")
                .portfolioName("Alpha Growth")
                .portfolioType(PortfolioType.EQUITY)
                .baseCurrency("USD")
                .aum(new BigDecimal("1000000.00"))
                .isActive(true)
                .build();

        ExposureSnapshot first = ExposureSnapshot.builder()
                .snapshotId(11)
                .portfolio(portfolio)
                .snapshotDate(LocalDate.of(2026, 3, 20))
                .totalExposure(new BigDecimal("1000.00"))
                .var1Day95(new BigDecimal("50.00"))
                .var1Day99(new BigDecimal("75.00"))
                .var10Day99(new BigDecimal("120.00"))
                .largestPositionPct(new BigDecimal("25.0000"))
                .currency("USD")
                .numPositions(4)
                .concentrationHerfindahl(new BigDecimal("2345.6789"))
                .build();

        ExposureSnapshot second = ExposureSnapshot.builder()
                .snapshotId(12)
                .portfolio(portfolio)
                .snapshotDate(LocalDate.of(2026, 3, 21))
                .totalExposure(new BigDecimal("1100.00"))
                .var1Day95(new BigDecimal("55.00"))
                .var1Day99(new BigDecimal("80.00"))
                .var10Day99(new BigDecimal("130.00"))
                .largestPositionPct(new BigDecimal("26.5000"))
                .currency("USD")
                .numPositions(5)
                .concentrationHerfindahl(new BigDecimal("2450.0000"))
                .build();

        when(portfolioRepository.findById(1)).thenReturn(Optional.of(portfolio));
        when(exposureSnapshotRepository.findByPortfolioPortfolioIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
                1,
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 21)))
                .thenReturn(List.of(first, second));

        GetSnapshotResponseDTO response = portfolioSnapshotService.getPortfolioSnapshots(
                1,
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 21));

        assertEquals(1, response.getPortfolioId());
        assertEquals("Alpha Growth", response.getPortfolioName());
        assertEquals(LocalDate.of(2026, 3, 20), response.getStartDate());
        assertEquals(LocalDate.of(2026, 3, 21), response.getEndDate());
        assertEquals(2, response.getSnapshots().size());

        ExposureSnapshotDTO dto = response.getSnapshots().get(0);
        assertEquals(11, dto.getSnapshotId());
        assertEquals(LocalDate.of(2026, 3, 20), dto.getSnapshotDate());
        assertEquals(new BigDecimal("1000.00"), dto.getTotalExposure());
        assertEquals(new BigDecimal("50.00"), dto.getVar1Day95());
        assertEquals(new BigDecimal("75.00"), dto.getVar1Day99());
        assertEquals(new BigDecimal("120.00"), dto.getVar10Day99());
        assertEquals(new BigDecimal("25.0000"), dto.getLargestPositionPct());
        assertEquals("USD", dto.getCurrency());
        assertEquals(4, dto.getNumPositions());
        assertEquals(new BigDecimal("2345.6789"), dto.getConcentrationHerfindahl());
    }
}
