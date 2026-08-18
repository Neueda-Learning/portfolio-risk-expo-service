package com.risk_busters.app.service;
import com.risk_busters.app.dto.PositionResponseDTO;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.model.Position;
import com.risk_busters.app.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PositionService {

    private final PositionRepository positionRepository;

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
}
