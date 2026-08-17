package com.risk_busters.app.service;

import com.risk_busters.app.dto.LimitCheckResultDTO;
import com.risk_busters.app.dto.VarResponseDTO;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.Position;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.repository.LimitRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Pure comparison service — calculates current metric values for every risk limit
 * belonging to a portfolio and produces {@link LimitCheckResultDTO} records.
 *
 * <p><strong>No database writes happen here.</strong>  Persistence is the
 * responsibility of {@link LimitBreachPersistenceService}.</p>
 *
 * <h3>Supported limit types</h3>
 * <ul>
 *   <li><b>VAR</b> — 1-day historical VaR (confidence derived from limitMetric:
 *       contains "99" → 99 %, otherwise 95 %)</li>
 *   <li><b>CONCENTRATION</b> — maximum single-position weight (weight_pct) across
 *       all positions in the portfolio, expressed as a percentage</li>
 *   <li><b>SECTOR_EXPOSURE</b> — if limitMetric names a specific sector, the
 *       exposure % for that sector; otherwise the maximum sector exposure %</li>
 *   <li><b>LEVERAGE</b> — total market value in base currency divided by AUM
 *       (leverage ratio, e.g. 1.25)</li>
 *   <li><b>DURATION / DRAWDOWN</b> — skipped (insufficient data in current model)</li>
 * </ul>
 *
 * <h3>Severity classification (for breaches)</h3>
 * <ul>
 *   <li>MINOR — utilisation 100 %–110 %</li>
 *   <li>MAJOR — utilisation 110 %–130 %</li>
 *   <li>CRITICAL — utilisation &gt; 130 %</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LimitComparisonService {


    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final LimitRepository limitRepository;
    private final PortfolioRiskService portfolioRiskService;

    public List<LimitCheckResultDTO> compareAllLimits(Integer portfolioId) {
        return compareAllLimitsInPortfolio(portfolioId);
    }

    public List<LimitCheckResultDTO> compareAllLimitsInPortfolio(Integer portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        return evaluatePortfolioLimits(portfolio);
    }

    public Map<Integer, List<LimitCheckResultDTO>> compareAllLimitsInAllPortfolios() {
        List<Portfolio> portfolios = portfolioRepository.findAll();

        Map<Integer, List<LimitCheckResultDTO>> resultsByPortfolio = new LinkedHashMap<>();
        for (Portfolio portfolio : portfolios) {
            resultsByPortfolio.put(portfolio.getPortfolioId(), evaluatePortfolioLimits(portfolio));
        }

        return resultsByPortfolio;
    }

    private List<LimitCheckResultDTO> evaluatePortfolioLimits(Portfolio portfolio) {
        Integer portfolioId = portfolio.getPortfolioId();
        List<Limit> limits = limitRepository.findByPortfolioPortfolioId(portfolioId);
        List<Position> positions = positionRepository.findByPortfolioPortfolioId(portfolioId);

        BigDecimal totalExposure = computeTotalExposure(positions);

        log.info("LimitCheck: portfolio={} limits={} positions={} totalExposure={}",
                portfolioId, limits.size(), positions.size(), totalExposure);

        return limits.stream()
                .map(limit -> evaluateLimit(limit, portfolio, positions, totalExposure, portfolioId))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Per-limit evaluation
    // -------------------------------------------------------------------------

    private LimitCheckResultDTO evaluateLimit(Limit limit,
                                              Portfolio portfolio,
                                              List<Position> positions,
                                              BigDecimal totalExposure,
                                              Integer portfolioId) {
        try {
            BigDecimal actualValue = computeActualValue(limit, portfolio, positions, totalExposure, portfolioId);

            if (actualValue == null) {
                return skippedResult(limit, portfolioId, "Metric not calculable from available data for type: "
                        + limit.getLimitType());
            }

            BigDecimal limitValue = limit.getLimitValue();
            boolean breached = limitValue != null && actualValue.compareTo(limitValue) > 0;
            boolean warning = !breached
                    && limit.getWarningThreshold() != null
                    && actualValue.compareTo(limit.getWarningThreshold()) > 0;

            BigDecimal utilisationPct = null;
            if (limitValue != null && limitValue.compareTo(BigDecimal.ZERO) > 0) {
                utilisationPct = actualValue
                        .divide(limitValue, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(4, RoundingMode.HALF_UP);
            }

            BigDecimal excessAmount = null;
            String severity = null;
            if (breached && limitValue != null) {
                excessAmount = actualValue.subtract(limitValue).setScale(4, RoundingMode.HALF_UP);
                severity = classifySeverity(utilisationPct);
            }

            log.debug("LimitCheck: limitId={} type={} actual={} limit={} utilisation={} breached={}",
                    limit.getLimitId(), limit.getLimitType(), actualValue, limitValue, utilisationPct, breached);

            return LimitCheckResultDTO.builder()
                    .limitId(limit.getLimitId())
                    .portfolioId(portfolioId)
                    .limitType(limit.getLimitType().name())
                    .limitMetric(limit.getLimitMetric())
                    .limitValue(limitValue)
                    .warningThreshold(limit.getWarningThreshold())
                    .actualValue(actualValue)
                    .utilisationPct(utilisationPct)
                    .breached(breached)
                    .warning(warning)
                    .excessAmount(excessAmount)
                    .severity(severity)
                    .skipped(false)
                    .build();

        } catch (Exception ex) {
            log.warn("LimitCheck: failed to evaluate limitId={} type={} — {}",
                    limit.getLimitId(), limit.getLimitType(), ex.getMessage());
            return skippedResult(limit, portfolioId, "Evaluation error: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Metric computations per LimitType
    // -------------------------------------------------------------------------

    /**
     * Dispatches to the correct metric calculator based on limit type.
     * Returns {@code null} for unsupported types (DURATION, DRAWDOWN).
     */
    private BigDecimal computeActualValue(Limit limit,
                                          Portfolio portfolio,
                                          List<Position> positions,
                                          BigDecimal totalExposure,
                                          Integer portfolioId) {
        return switch (limit.getLimitType()) {
            case VAR             -> computeVar(portfolioId, limit.getLimitMetric());
            case CONCENTRATION   -> computeMaxConcentration(positions);
            case SECTOR_EXPOSURE -> computeSectorExposure(positions, totalExposure, limit.getLimitMetric());
            case LEVERAGE        -> computeLeverage(totalExposure, portfolio.getAum());
            // Not enough data in current model to compute these reliably
            case DURATION, DRAWDOWN -> null;
        };
    }

    /**
     * 1-day historical VaR using {@link PortfolioRiskService}.
     * Confidence level is inferred from limitMetric ("99" → 99 %, else 95 %).
     */
    private BigDecimal computeVar(Integer portfolioId, String limitMetric) {
        int confidence = (limitMetric != null && limitMetric.contains("99")) ? 99 : 95;
        VarResponseDTO varResult = portfolioRiskService.calculate1DayVar(portfolioId, confidence);
        return varResult.getVar1Day();
    }

    /**
     * Maximum single-position weight (weight_pct) expressed as a percentage.
     * E.g. a position with weight_pct = 18.5 returns 18.5.
     */
    private BigDecimal computeMaxConcentration(List<Position> positions) {
        return positions.stream()
                .map(Position::getWeightPct)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Sector exposure as a percentage of total portfolio market value.
     *
     * <ul>
     *   <li>If {@code limitMetric} matches an existing sector name exactly,
     *       returns that sector's exposure %.</li>
     *   <li>Otherwise returns the maximum sector exposure % across all sectors.</li>
     * </ul>
     */
    private BigDecimal computeSectorExposure(List<Position> positions,
                                              BigDecimal totalExposure,
                                              String limitMetric) {
        if (totalExposure == null || totalExposure.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        Map<String, BigDecimal> sectorValues = positions.stream()
                .filter(p -> p.getMarketValueBase() != null)
                .collect(Collectors.groupingBy(
                        p -> {
                            if (p.getInstrument() == null
                                    || p.getInstrument().getSector() == null
                                    || p.getInstrument().getSector().isBlank()) {
                                return "UNASSIGNED";
                            }
                            return p.getInstrument().getSector();
                        },
                        Collectors.reducing(BigDecimal.ZERO, Position::getMarketValueBase, BigDecimal::add)
                ));

        // If a specific sector is targeted by this limit, use only that sector
        if (limitMetric != null && !limitMetric.isBlank() && sectorValues.containsKey(limitMetric)) {
            return sectorValues.get(limitMetric)
                    .divide(totalExposure, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(4, RoundingMode.HALF_UP);
        }

        // Otherwise compare the worst (largest) sector
        return sectorValues.values().stream()
                .map(v -> v.divide(totalExposure, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(4, RoundingMode.HALF_UP))
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Leverage ratio = totalMarketValueBase / AUM.
     * E.g. a ratio of 1.25 means the portfolio is 125 % invested.
     */
    private BigDecimal computeLeverage(BigDecimal totalExposure, BigDecimal aum) {
        if (aum == null || aum.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalExposure.divide(aum, 4, RoundingMode.HALF_UP);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private BigDecimal computeTotalExposure(List<Position> positions) {
        return positions.stream()
                .map(Position::getMarketValueBase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Classifies breach severity based on utilisation percentage.
     *
     * <ul>
     *   <li>100 %–110 % → MINOR</li>
     *   <li>110 %–130 % → MAJOR</li>
     *   <li>&gt; 130 %  → CRITICAL</li>
     * </ul>
     */
    private String classifySeverity(BigDecimal utilisationPct) {
        if (utilisationPct == null) return "MINOR";
        double pct = utilisationPct.doubleValue();
        if (pct > 130) return "CRITICAL";
        if (pct > 110) return "MAJOR";
        return "MINOR";
    }

    private LimitCheckResultDTO skippedResult(Limit limit, Integer portfolioId, String reason) {
        return LimitCheckResultDTO.builder()
                .limitId(limit.getLimitId())
                .portfolioId(portfolioId)
                .limitType(limit.getLimitType() != null ? limit.getLimitType().name() : null)
                .limitMetric(limit.getLimitMetric())
                .limitValue(limit.getLimitValue())
                .warningThreshold(limit.getWarningThreshold())
                .skipped(true)
                .skipReason(reason)
                .build();
    }
}
