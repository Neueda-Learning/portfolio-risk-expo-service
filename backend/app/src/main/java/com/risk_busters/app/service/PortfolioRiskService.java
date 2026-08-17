package com.risk_busters.app.service;

import com.risk_busters.app.dto.*;
import com.risk_busters.app.exceptions.InsufficientPriceHistoryException;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.mapper.LimitMapper;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.PriceHistory;
import com.risk_busters.app.model.Position;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.repository.LimitRepository;
import com.risk_busters.app.repository.PriceHistoryRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioRiskService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioRiskService.class);
    
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final LimitRepository limitRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final LimitMapper limitMapper;
    
    /**
     * Calculate total exposure for a portfolio by summing position values
     */
    public ExposureResponseDTO calculateExposure(Integer portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));
        
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
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));
        
        // Calculate current exposure
        ExposureResponseDTO exposure = calculateExposure(portfolioId);
        BigDecimal totalExposure = exposure.getTotalExposure();
        
        // Get all limits for the portfolio
        List<Limit> limits = limitRepository.findByPortfolioPortfolioId(portfolioId);
        
        List<LimitDetailDTO> limitDetails = limits.stream()
                .map(limit -> limitMapper.toDto(limit, totalExposure))
                .toList();
        
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
                logger.error("VaR calculation failed: portfolio={} instrument={} reason=\"Insufficient price history\" availableDays={} requiredDays=252",
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
                logger.error("VaR calculation failed: portfolio={} instrument={} reason=\"Insufficient valid close prices\" availablePrices={} requiredPrices=252",
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

    private double calculate1DayHistoricalVaR(List<Double> historicalPrices,
                                               double currentExposure,
                                               int confidenceLevel,
                                               String portfolioId) {

        if (historicalPrices == null || historicalPrices.size() != 252) {
            logger.error("VaR calculation failed: portfolio={} reason=\"Insufficient price history\"", portfolioId);
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

        logger.info("VaR calculated: portfolio={} var1Day_{}={} historyDays={}",
                portfolioId, confidenceLevel, varValue, historicalPrices.size());

        return varValue;

    }

    @Transactional
    public void storeSnapshot(Integer portfolioId, LocalDate snapshotDate) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        try {
            portfolioRepository.storeSnapshot(portfolioId, snapshotDate);
            logger.info("Snapshot stored: portfolio={} snapshotDate={}", portfolioId, snapshotDate);
        } catch (Exception e) {
            logger.warn("Snapshot creation failed: portfolio={} snapshotDate={} reason={}", portfolioId, snapshotDate, e.getMessage());
            throw new RuntimeException(e); //TODO proper exception
        }

    }
}

