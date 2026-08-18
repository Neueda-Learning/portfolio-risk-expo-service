package com.risk_busters.app.service;

import com.risk_busters.app.dto.LimitCheckResultDTO;
import com.risk_busters.app.model.AssetClass;
import com.risk_busters.app.model.Instrument;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitBreach;
import com.risk_busters.app.model.LimitBreachStatus;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.model.LimitType;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.model.PortfolioType;
import com.risk_busters.app.repository.LimitBreachRepository;
import com.risk_busters.app.repository.LimitRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimitBreachPersistenceServiceTest {

    @Mock
    private LimitBreachRepository limitBreachRepository;
    @Mock
    private LimitRepository limitRepository;
    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private LimitBreachPersistenceService service;

    @Test
    void persistBreaches_createsOpenBreachWithCorrectFields() {
        Portfolio portfolio = portfolio();
        Limit limit = limit(portfolio);
        LimitCheckResultDTO result = LimitCheckResultDTO.builder()
                .limitId(10)
                .portfolioId(1)
                .limitType(LimitType.SECTOR_CONC.name())
                .limitMetric("Technology")
                .limitValue(new BigDecimal("25.00"))
                .warningThreshold(new BigDecimal("20.00"))
                .actualValue(new BigDecimal("30.00"))
                .utilisationPct(new BigDecimal("120.0000"))
                .breached(true)
                .warning(false)
                .excessAmount(new BigDecimal("5.0000"))
                .severity("MAJOR")
                .skipped(false)
                .build();

        when(portfolioRepository.findById(1)).thenReturn(Optional.of(portfolio));
        when(limitRepository.findById(10)).thenReturn(Optional.of(limit));
        when(limitBreachRepository.findMaxBreachId()).thenReturn(99);
        when(limitBreachRepository.existsByLimitLimitIdAndBreachDate(10, LocalDate.now())).thenReturn(false);
        when(limitBreachRepository.save(any(LimitBreach.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int inserted = service.persistBreaches(1, List.of(result));

        Assertions.assertEquals(1, inserted);

        ArgumentCaptor<Limit> limitCaptor = ArgumentCaptor.forClass(Limit.class);
        verify(limitRepository).save(limitCaptor.capture());
        Assertions.assertEquals(LimitStatus.BREACH, limitCaptor.getValue().getStatus());
        Assertions.assertEquals(0, limitCaptor.getValue().getCurrentValue().compareTo(new BigDecimal("30.00")));
        Assertions.assertEquals(0, limitCaptor.getValue().getUtilisationPct().compareTo(new BigDecimal("120.0000")));

        ArgumentCaptor<LimitBreach> breachCaptor = ArgumentCaptor.forClass(LimitBreach.class);
        verify(limitBreachRepository).save(breachCaptor.capture());
        LimitBreach breach = breachCaptor.getValue();
        Assertions.assertEquals(Integer.valueOf(100), breach.getBreachId());
        Assertions.assertEquals(LimitBreachStatus.OPEN, breach.getStatus());
        Assertions.assertEquals(0, breach.getLimitValue().compareTo(new BigDecimal("25.00")));
        Assertions.assertEquals(0, breach.getActualValue().compareTo(new BigDecimal("30.00")));
        Assertions.assertEquals(0, breach.getExcessAmount().compareTo(new BigDecimal("5.00")));
        Assertions.assertEquals("MAJOR", breach.getSeverity());
        Assertions.assertEquals(portfolio, breach.getPortfolio());
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

    private static Limit limit(Portfolio portfolio) {
        return Limit.builder()
                .limitId(10)
                .portfolio(portfolio)
                .limitType(LimitType.SECTOR_CONC)
                .limitMetric("Technology")
                .limitValue(new BigDecimal("25.00"))
                .warningThreshold(new BigDecimal("20.00"))
                .currentValue(new BigDecimal("30.00"))
                .utilisationPct(new BigDecimal("120.0000"))
                .status(LimitStatus.OK)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();
    }
}
