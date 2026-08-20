package com.risk_busters.app.service;

import com.risk_busters.app.dto.InstrumentDTO;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.model.AssetClass;
import com.risk_busters.app.model.Instrument;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.repository.InstrumentRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    private PositionService service;

    @BeforeEach
    void setUp() {
        service = new PositionService(positionRepository, instrumentRepository, portfolioRepository);
    }

    @Test
    void getInstrumentInPortfolioByPositionId_returnsInstrumentForMatchingPortfolioPosition() {
        when(portfolioRepository.findById(10)).thenReturn(Optional.of(Portfolio.builder().portfolioId(10).build()));

        Instrument instrument = Instrument.builder()
                .instrumentId(200)
                .instrumentIsin("US0000000200")
                .instrumentName("Treasury Bond 2030")
                .currency("USD")
                .issueDate(LocalDate.of(2024, 1, 10))
                .maturityDate(LocalDate.of(2030, 1, 10))
                .issuer("US Treasury")
                .sector("Government")
                .assetClass(AssetClass.builder()
                        .assetClassId(2)
                        .assetClassName("Fixed Income")
                        .build())
                .isActive(Boolean.TRUE)
                .createdAt(LocalDateTime.of(2026, 3, 20, 9, 30))
                .build();

        when(instrumentRepository.findByPositionIdAndPortfolioId(10, 55)).thenReturn(Optional.of(instrument));

        InstrumentDTO result = service.getInstrumentInPortfolioByPositionId(10, 55);

        Assertions.assertEquals(200, result.getInstrumentId());
        Assertions.assertEquals("US0000000200", result.getInstrumentIsIn());
        Assertions.assertEquals("Treasury Bond 2030", result.getInstrumentName());
        Assertions.assertEquals("USD", result.getCurrency());
        Assertions.assertEquals("Fixed Income", result.getAssetClass());
        Assertions.assertEquals("2", result.getAssetClassId());
        Assertions.assertTrue(result.getIsActive());
    }

    @Test
    void getInstrumentInPortfolioByPositionId_throwsWhenPositionIsMissingFromPortfolio() {
        when(portfolioRepository.findById(10)).thenReturn(Optional.of(Portfolio.builder().portfolioId(10).build()));
        when(instrumentRepository.findByPositionIdAndPortfolioId(10, 99)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.getInstrumentInPortfolioByPositionId(10, 99));
    }

    @Test
    void getInstrumentInPortfolioByPositionId_throwsWhenPortfolioDoesNotExist() {
        when(portfolioRepository.findById(77)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.getInstrumentInPortfolioByPositionId(77, 55));
    }
}
