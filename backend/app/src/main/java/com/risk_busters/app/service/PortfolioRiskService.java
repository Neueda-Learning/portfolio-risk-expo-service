package com.risk_busters.app.service;

import com.risk_busters.app.dto.*;
import com.risk_busters.app.exceptions.InsufficientPriceHistoryException;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.mapper.LimitMapper;
import com.risk_busters.app.model.*;
import com.risk_busters.app.exception.PortfolioNotFoundException;
import com.risk_busters.app.repository.LimitRepository;
import com.risk_busters.app.repository.PriceHistoryRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioRiskService {

    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final LimitRepository limitRepository;
    private final PriceHistoryRepository priceHistoryRepository;
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
     * Calculate total exposure grouped by instrument sector for a portfolio.
     */
    public SectorExposureResponseDTO calculateExposureBySector(Integer portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        List<Position> positions = positionRepository.findByPortfolioPortfolioId(portfolioId);

        Map<String, BigDecimal> sectorExposures = positions.stream()
                .filter(position -> position.getMarketValueBase() != null)
                .collect(Collectors.groupingBy(
                        position -> {
                            if (position.getInstrument() == null
                                    || position.getInstrument().getSector() == null
                                    || position.getInstrument().getSector().isBlank()) {
                                return "UNASSIGNED";
                            }
                            return position.getInstrument().getSector();
                        },
                        Collectors.reducing(BigDecimal.ZERO, Position::getMarketValueBase, BigDecimal::add)
                ));

        return SectorExposureResponseDTO.builder()
                .portfolioId(portfolioId)
                .portfolioName(portfolio.getPortfolioName())
                .sectorExposures(sectorExposures)
                .build();
    }

    /**
     * Get portfolio with limits and current utilisation
     */
    public PortfolioLimitsResponseDTO getPortfolioLimits(Integer portfolioId) {
        Instant startedAt = Instant.now();
        Portfolio portfolio = loadPortfolio(portfolioId);
        
        // Calculate current exposure
        ExposureResponseDTO exposure = calculateExposure(portfolioId);
        BigDecimal totalExposure = exposure.getTotalExposure();
        
        // Get all limits for the portfolio
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
    public AssetExposureResponseDTO calculateExposureByAsset(Integer portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        List<Position> positions = positionRepository.findByPortfolioPortfolioId(portfolioId);

        Map<String, BigDecimal> assetsExposuresMap = positions.stream()
                .filter(position -> position.getMarketValueBase() != null)
                .collect(Collectors.groupingBy(
                        position -> {
                            if (position.getInstrument() == null
                                    || position.getInstrument().getAssetClass() == null) {
                                return "NOT CLASSIFIED";
                            }
                            return position.getInstrument().getAssetClass().getAssetClassName();
                        },
                        Collectors.reducing(BigDecimal.ZERO, Position::getMarketValueBase, BigDecimal::add)
                ));

        return AssetExposureResponseDTO.builder()
                .portfolioId(portfolioId)
                .portfolioName(portfolio.getPortfolioName())
                .assetExposures(assetsExposuresMap)
                .build();
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public VarResponseDTO calculate1DayVar(Integer portfolioId, Integer confidence) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        int confidenceLevel = confidence == null ? 95 : confidence;
        if (confidenceLevel != 95 && confidenceLevel != 99) {
            throw new IllegalArgumentException("Confidence must be either 95 or 99.");
        }

        List<Position> positions = positionRepository.findByPortfolioPortfolioId(portfolioId);

        BigDecimal totalVar = BigDecimal.ZERO;
        int contributingPositions = 0;

        for (Position position : positions) {
            if (position.getInstrument() == null
                    || position.getInstrument().getInstrumentId() == null
                    || position.getMarketValueBase() == null
                    || position.getMarketValueBase().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            List<PriceHistory> priceHistoryDesc = priceHistoryRepository
                    .findByInstrumentInstrumentIdOrderByPriceDateDesc(position.getInstrument().getInstrumentId());

            if (priceHistoryDesc.size() < 252) {
                log.error("VaR calculation failed: portfolio={} instrument={} reason=\"Insufficient price history\" availableDays={} requiredDays=252",
                        portfolioId,
                        position.getInstrument().getInstrumentId(),
                        priceHistoryDesc.size());
                throw new InsufficientPriceHistoryException(
                        position.getInstrument().getInstrumentId(),
                        priceHistoryDesc.size(),
                        252);
            }

            List<Double> historicalPrices = priceHistoryDesc.stream()
                    .limit(252)
                    .map(PriceHistory::getClosePrice)
                    .filter(Objects::nonNull)
                    .map(BigDecimal::doubleValue)
                    .collect(Collectors.toList());

            // The VaR helper expects chronological order (oldest to newest).
            Collections.reverse(historicalPrices);

            if (historicalPrices.size() < 252) {
                log.error("VaR calculation failed: portfolio={} instrument={} reason=\"Insufficient valid close prices\" availablePrices={} requiredPrices=252",
                        portfolioId,
                        position.getInstrument().getInstrumentId(),
                        historicalPrices.size());
                throw new InsufficientPriceHistoryException(
                        position.getInstrument().getInstrumentId(),
                        historicalPrices.size(),
                        252);
            }

            double positionVar = calculate1DayHistoricalVaR(
                    historicalPrices,
                    position.getMarketValueBase().doubleValue(),
                    confidenceLevel,
                    String.valueOf(portfolioId)
            );

            totalVar = totalVar.add(BigDecimal.valueOf(positionVar));
            contributingPositions++;
        }

        if (contributingPositions == 0) {
            throw new IllegalArgumentException("No valid positions available to calculate VaR.");
        }

        return VarResponseDTO.builder()
                .portfolioId(portfolioId)
                .portfolioName(portfolio.getPortfolioName())
                .var1Day(totalVar.setScale(2, RoundingMode.HALF_UP))
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

    private double calculate1DayHistoricalVaR(List<Double> historicalPrices,
                                               double currentExposure,
                                               int confidenceLevel,
                                               String portfolioId) {

        if (historicalPrices == null || historicalPrices.size() != 252) {
            log.error("VaR calculation failed: portfolio={} reason=\"Insufficient price history\"", portfolioId);
            throw new IllegalArgumentException("Exactly 252 historical prices are required to calculate VaR.");
        }

        List<Double> dailyReturns = new ArrayList<>();
        for (int i = 1; i < historicalPrices.size(); i++) {
            double previousPrice = historicalPrices.get(i - 1);
            double currentPrice = historicalPrices.get(i);

            if (previousPrice == 0) {
                continue;
            }

            double dailyReturn = (currentPrice - previousPrice) / previousPrice;
            dailyReturns.add(dailyReturn);
        }

        if (dailyReturns.isEmpty()) {
            return 0.0;
        }

        Collections.sort(dailyReturns);

        double significanceLevel = 1.0 - (confidenceLevel / 100.0);
        int index = (int) Math.max(0, Math.ceil(dailyReturns.size() * significanceLevel) - 1);

        double percentileReturn = dailyReturns.get(index);
        double varPercentage = percentileReturn < 0 ? Math.abs(percentileReturn) : 0.0;
        double varValue = varPercentage * currentExposure;

        log.info("VaR calculated: portfolio={} var1Day_{}={} historyDays={}",
                portfolioId, confidenceLevel, varValue, historicalPrices.size());

        return varValue;

    }
    private Portfolio loadPortfolio(Integer portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> {
                    log.error("Portfolio lookup failed: portfolioId={} reason=Portfolio not found", portfolioId);
                    return new PortfolioNotFoundException(portfolioId);
                });
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
}

