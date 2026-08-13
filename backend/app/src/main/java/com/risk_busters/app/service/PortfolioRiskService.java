package com.risk_busters.app.service;

import com.risk_busters.app.dto.ExposureResponseDTO;
import com.risk_busters.app.dto.LimitDetailDTO;
import com.risk_busters.app.dto.PortfolioLimitsResponseDTO;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.model.Position;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.repository.LimitRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioRiskService {
    
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final LimitRepository limitRepository;
    
    /**
     * Calculate total exposure for a portfolio by summing position values
     */
    public ExposureResponseDTO calculateExposure(Integer portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        
        List<Position> positions = positionRepository.findByPortfolioPortfolioId(portfolioId);
        BigDecimal totalExposure = positions.stream()
                .map(Position::getMarketValueBase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
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
        BigDecimal currentValue = limit.getCurrentValue() != null ? limit.getCurrentValue() : totalExposure;
        BigDecimal utilisationPct = limit.getUtilisationPct();

        if (utilisationPct == null
                && currentValue != null
                && limit.getLimitValue() != null
                && limit.getLimitValue().compareTo(BigDecimal.ZERO) > 0) {
            utilisationPct = currentValue
                    .divide(limit.getLimitValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        boolean isBreached = LimitStatus.BREACH.equals(limit.getStatus())
                || (currentValue != null
                && limit.getLimitValue() != null
                && currentValue.compareTo(limit.getLimitValue()) > 0);
        
        return LimitDetailDTO.builder()
                .limitId(limit.getLimitId())
                .limitType(limit.getLimitType() != null ? limit.getLimitType().name() : null)
                .limitMetric(limit.getLimitMetric())
                .limitValue(limit.getLimitValue())
                .warningThreshold(limit.getWarningThreshold())
                .currentValue(currentValue)
                .utilisationPct(utilisationPct)
                .status(limit.getStatus() != null ? limit.getStatus().name() : null)
                .effectiveFrom(limit.getEffectiveFrom())
                .effectiveTo(limit.getEffectiveTo())
                .isBreached(isBreached)
                .build();
    }
}

