package com.risk_busters.app.service;

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
}

