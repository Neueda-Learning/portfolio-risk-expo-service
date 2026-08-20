package com.risk_busters.app.service;
import com.risk_busters.app.dto.InstrumentDTO;
import com.risk_busters.app.dto.PositionResponseDTO;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.model.Position;
import com.risk_busters.app.repository.InstrumentRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PositionService {

    private final PositionRepository positionRepository;
    private final InstrumentRepository instrumentRepository;
    private final PortfolioRepository portfolioRepository;

    /**
     * Get a position by ID and verify it belongs to the specified portfolio
     */
    public PositionResponseDTO getPositionByIdFromPortfolio(Integer portfolioId, Integer positionId) {
        log.debug("Retrieving position: portfolioId={} positionId={}", portfolioId, positionId);
        
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> {
                    log.error("Position not found: positionId={}", positionId);
                    return new ResourceNotFoundException("Position not found with id: " + positionId);
                });

        if (!position.getPortfolio().getPortfolioId().equals(portfolioId)) {
            log.error("Position does not belong to portfolio: positionId={} portfolioId={}", positionId, portfolioId);
            throw new ResourceNotFoundException("Position " + positionId + " does not belong to portfolio " + portfolioId);
        }

        return PositionResponseDTO.builder()
                .portfolioId(position.getPortfolio().getPortfolioId())
                .positionId(position.getPositionId())
                .instrumentId(position.getInstrument().getInstrumentId())
                .instrumentName(position.getInstrument().getInstrumentName())
                .quantity(position.getQuantity())
                .marketPrice(position.getMarketPrice())
                .marketValue(position.getMarketValue())
                .marketValueBase(position.getMarketValueBase())
                .weightPct(position.getWeightPct())
                .costBasis(position.getCostBasis())
                .positionDate(position.getPositionDate())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }

    public List<PositionResponseDTO> getAllPositionsByIdFromPortfolio(Integer portfolioId) {
        log.debug("Retrieving all position for portfolioId={} ", portfolioId);
        List<Position> positions = positionRepository.findAllByPortfolioPortfolioId(portfolioId);

        if (positions.isEmpty()) {
            log.error("No positions found for portfolioId={}", portfolioId);
            throw new ResourceNotFoundException("No positions found for portfolio with id: " + portfolioId);
        }

        return positions.stream()
                .map(position -> PositionResponseDTO.builder()
                        .portfolioId(position.getPortfolio().getPortfolioId())
                        .positionId(position.getPositionId())
                        .instrumentId(position.getInstrument().getInstrumentId())
                        .instrumentName(position.getInstrument().getInstrumentName())
                        .quantity(position.getQuantity())
                        .marketPrice(position.getMarketPrice())
                        .marketValue(position.getMarketValue())
                        .marketValueBase(position.getMarketValueBase())
                        .weightPct(position.getWeightPct())
                        .costBasis(position.getCostBasis())
                        .positionDate(position.getPositionDate())
                        .createdAt(position.getCreatedAt())
                        .updatedAt(position.getUpdatedAt())
                        .build())
                .toList();
    }

    public InstrumentDTO getInstrumentInPortfolioByPositionId(Integer portfolioId, Integer positionId) {
        log.debug("Retrieving an instrument for portfolioId={} at positionId={}", portfolioId, positionId);

        portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> {
                    log.error("Portfolio not found: portfolioId={}", portfolioId);
                    return new ResourceNotFoundException("Portfolio not found with id: " + portfolioId);
                });

        var instrument = instrumentRepository.findByPositionIdAndPortfolioId(portfolioId, positionId)
                .orElseThrow(() -> {
                    log.error("Instrument not found for portfolioId={} positionId={}", portfolioId, positionId);
                    return new ResourceNotFoundException(
                            "No instrument found for position " + positionId + " in portfolio " + portfolioId
                    );
                });

        return InstrumentDTO.builder()
                .instrumentId(instrument.getInstrumentId())
                .instrumentIsIn(instrument.getInstrumentIsin())
                .instrumentName(instrument.getInstrumentName())
                .currency(instrument.getCurrency())
                .issueDate(instrument.getIssueDate())
                .maturityDate(instrument.getMaturityDate())
                .issuer(instrument.getIssuer())
                .sector(instrument.getSector())
                .assetClass(instrument.getAssetClass().getAssetClassName())
                .assetClassId(String.valueOf(instrument.getAssetClass().getAssetClassId()))
                .isActive(instrument.getIsActive())
                .createdAt(instrument.getCreatedAt())
                .build();
    }
}
