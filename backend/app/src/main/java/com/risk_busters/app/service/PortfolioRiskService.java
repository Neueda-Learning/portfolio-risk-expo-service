package com.risk_busters.app.service;

import com.risk_busters.app.dto.ExposureResponseDTO;
import com.risk_busters.app.dto.LimitDetailDTO;
import com.risk_busters.app.dto.PortfolioLimitsResponseDTO;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.Position;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.repository.LimitRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import com.risk_busters.app.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioRiskService {
    
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final LimitRepository limitRepository;
    
    /**
     * Calculate total exposure for a portfolio by summing position values
     */
    public ExposureResponseDTO calculateExposure(Integer portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        
        List<Position> positions = positionRepository.findByPortfolioPortfolioId(portfolioId);
        BigDecimal totalExposure = BigDecimal.ZERO;
        
        for (Position position : positions) {
            // Get latest price for the instrument
            var priceOptional = priceHistoryRepository.findLatestPriceByInstrumentId(position.getInstrument().getInstrumentId());
            if (priceOptional.isPresent()) {
                BigDecimal price = priceOptional.get().getClosePrice();
                BigDecimal positionValue = position.getQuantity().multiply(price);
                totalExposure = totalExposure.add(positionValue);
            }
        }
        
        Integer positionCount = positionRepository.countByPortfolioId(portfolioId);
        
        return ExposureResponseDTO.builder()
                .portfolioId(portfolioId)
                .portfolioName(portfolio.getPortfolioName())
                .totalExposure(totalExposure)
                .currency(portfolio.getBaseCurrency())
                .positionCount(positionCount)
                .build();
    }
    
    /**
     * Get portfolio with limits and current utilisation
     */
    public PortfolioLimitsResponseDTO getPortfolioLimits(Integer portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        
        // Calculate current exposure
        ExposureResponseDTO exposure = calculateExposure(portfolioId);
        BigDecimal totalExposure = exposure.getTotalExposure();
        
        // Get all limits for the portfolio
        List<Limit> limits = limitRepository.findByPortfolioPortfolioId(portfolioId);
        
        List<LimitDetailDTO> limitDetails = limits.stream()
                .map(limit -> buildLimitDetail(limit, totalExposure))
                .collect(Collectors.toList());
        
        return PortfolioLimitsResponseDTO.builder()
                .portfolioId(portfolioId)
                .portfolioName(portfolio.getPortfolioName())
                .totalExposure(totalExposure)
                .baseCurrency(portfolio.getBaseCurrency())
                .limits(limitDetails)
                .build();
    }
    
    /**
     * Build limit detail with current utilisation
     */
    private LimitDetailDTO buildLimitDetail(Limit limit, BigDecimal totalExposure) {
        BigDecimal currentUtilisation = totalExposure;
        BigDecimal utilisationPct = BigDecimal.ZERO;
        boolean isBreached = false;
        
        if (limit.getLimitValue().compareTo(BigDecimal.ZERO) > 0) {
            utilisationPct = currentUtilisation
                    .divide(limit.getLimitValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            isBreached = currentUtilisation.compareTo(limit.getLimitValue()) > 0;
        }
        
        return LimitDetailDTO.builder()
                .limitId(limit.getLimitId())
                .limitType(limit.getLimitType())
                .limitValue(limit.getLimitValue())
                .currentUtilisation(currentUtilisation)
                .utilisationPct(utilisationPct)
                .warningPct(limit.getWarningPct())
                .isBreached(isBreached)
                .currency(limit.getCurrency())
                .build();
    }
}

