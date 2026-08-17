package com.risk_busters.app.service;

import com.risk_busters.app.dto.ExposureResponseDTO;
import com.risk_busters.app.dto.LimitDetailDTO;
import com.risk_busters.app.dto.PortfolioLimitsResponseDTO;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.model.Position;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.exception.PortfolioNotFoundException;
import com.risk_busters.app.repository.LimitRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioRiskService {
    
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final LimitRepository limitRepository;
    
    /**
     * Calculate total exposure for a portfolio by summing position values
     */
    public ExposureResponseDTO calculateExposure(Integer portfolioId) {
        Instant startedAt = Instant.now();
        Portfolio portfolio = loadPortfolio(portfolioId);
        
        List<Position> positions = positionRepository.findByPortfolioPortfolioId(portfolioId);
        long excludedPositions = positions.stream()
                .filter(position -> position.getMarketValueBase() == null)
                .count();
        BigDecimal totalExposure = positions.stream()
                .map(Position::getMarketValueBase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Integer positionCount = positionRepository.countByPortfolioId(portfolioId);
        LocalDate asOfDate = positions.stream()
                .map(Position::getPositionDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        if (excludedPositions > 0) {
            log.warn(
                    "Price missing for position: portfolioId={} portfolioCode={} excludedPositions={} reason=Missing marketValueBase",
                    portfolio.getPortfolioId(),
                    portfolio.getPortfolioCode(),
                    excludedPositions
            );
        }

        log.info(
                "Exposure calculated: portfolioId={} portfolioCode={} portfolioName={} totalExposure={} {} positions={} asOfDate={} elapsed={}ms",
                portfolio.getPortfolioId(),
                portfolio.getPortfolioCode(),
                portfolio.getPortfolioName(),
                totalExposure,
                portfolio.getBaseCurrency(),
                positionCount,
                asOfDate,
                Duration.between(startedAt, Instant.now()).toMillis()
        );
        
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
        Instant startedAt = Instant.now();
        Portfolio portfolio = loadPortfolio(portfolioId);
        
        ExposureResponseDTO exposure = calculateExposure(portfolioId);
        BigDecimal totalExposure = exposure.getTotalExposure();
        
        List<Limit> limits = limitRepository.findByPortfolioPortfolioId(portfolioId);
        List<LimitDetailDTO> limitDetails = new ArrayList<>();
        long breachCount = 0;
        long warningCount = 0;

        for (Limit limit : limits) {
            LimitDetailDTO detail = buildLimitDetail(limit, totalExposure);
            limitDetails.add(detail);

            if (Boolean.TRUE.equals(detail.getIsBreached())) {
                breachCount++;
                log.warn(
                        "Limit breach: portfolioId={} portfolioCode={} limitId={} limitType={} currentValue={} limitValue={} status={}",
                        portfolio.getPortfolioId(),
                        portfolio.getPortfolioCode(),
                        detail.getLimitId(),
                        detail.getLimitType(),
                        detail.getCurrentValue(),
                        detail.getLimitValue(),
                        detail.getStatus()
                );
                continue;
            }

            if (isWarningApproaching(detail)) {
                warningCount++;
                log.warn(
                        "Warning threshold approaching: portfolioId={} portfolioCode={} limitId={} limitType={} currentValue={} warningThreshold={} utilisationPct={} status={}",
                        portfolio.getPortfolioId(),
                        portfolio.getPortfolioCode(),
                        detail.getLimitId(),
                        detail.getLimitType(),
                        detail.getCurrentValue(),
                        detail.getWarningThreshold(),
                        detail.getUtilisationPct(),
                        detail.getStatus()
                );
            }
        }

        log.info(
                "Limits calculated: portfolioId={} portfolioCode={} portfolioName={} totalExposure={} {} limits={} warnings={} breaches={} elapsed={}ms",
                portfolio.getPortfolioId(),
                portfolio.getPortfolioCode(),
                portfolio.getPortfolioName(),
                totalExposure,
                portfolio.getBaseCurrency(),
                limitDetails.size(),
                warningCount,
                breachCount,
                Duration.between(startedAt, Instant.now()).toMillis()
        );
        
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

    private boolean isWarningApproaching(LimitDetailDTO detail) {
        if (Boolean.TRUE.equals(detail.getIsBreached())) {
            return false;
        }

        if ("WARNING".equals(detail.getStatus())) {
            return true;
        }

        return detail.getUtilisationPct() != null
                && detail.getUtilisationPct().compareTo(new BigDecimal("90")) >= 0;
    }

    private Portfolio loadPortfolio(Integer portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> {
                    log.error("Portfolio lookup failed: portfolioId={} reason=Portfolio not found", portfolioId);
                    return new PortfolioNotFoundException(portfolioId);
                });
    }
}
