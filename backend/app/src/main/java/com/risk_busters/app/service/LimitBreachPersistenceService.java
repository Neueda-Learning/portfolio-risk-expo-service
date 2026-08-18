package com.risk_busters.app.service;

import com.risk_busters.app.dto.LimitCheckResultDTO;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitBreach;
import com.risk_busters.app.model.LimitBreachStatus;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.repository.LimitBreachRepository;
import com.risk_busters.app.repository.LimitRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Persistence service responsible for writing limit-breach records and updating
 * limit statuses based on comparison results produced by {@link LimitComparisonService}.
 *
 * <p>This service is intentionally separate from the comparison logic so that
 * the two concerns (calculation vs. persistence) can be tested and reasoned about
 * independently.</p>
 *
 * <h3>Behaviour per result</h3>
 * <ul>
 *   <li>Skipped results are ignored entirely.</li>
 *   <li>For every non-skipped result the {@code risk_limit.status} and
 *       {@code risk_limit.current_value} / {@code utilisation_pct} columns are
 *       updated to reflect the freshly computed values.</li>
 *   <li>A new {@link LimitBreach} record with status {@code OPEN} is inserted
 *       only when the limit is breached <em>and</em> no open breach already exists
 *       for the same limit on today's date (idempotent re-runs).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LimitBreachPersistenceService {


    private final LimitBreachRepository limitBreachRepository;
    private final LimitRepository limitRepository;
    private final PortfolioRepository portfolioRepository;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Persist breach records and update limit statuses for the supplied results.
     *
     * @param portfolioId the portfolio these results belong to
     * @param results     comparison results from {@link LimitComparisonService#compareAllLimitsInPortfolio}
     * @return the number of <em>new</em> {@link LimitBreach} rows inserted
     * @throws ResourceNotFoundException if the portfolio does not exist
     */
    @Transactional
    public int persistBreaches(Integer portfolioId, List<LimitCheckResultDTO> results) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        LocalDate today = LocalDate.now();
        int nextBreachId = limitBreachRepository.findMaxBreachId() + 1;
        int newBreaches = 0;

        for (LimitCheckResultDTO result : results) {
            if (result.isSkipped()) {
                continue;
            }

            Limit limit = limitRepository.findById(result.getLimitId()).orElse(null);
            if (limit == null) {
                log.warn("LimitBreachPersistence: limitId={} not found, skipping", result.getLimitId());
                continue;
            }

            // --- Update the live limit record ---
            updateLimitRecord(limit, result);

            // --- Insert a breach record if breached and not already recorded today ---
            if (!result.isBreached()) {
                continue;
            }

            boolean alreadyOpen = limitBreachRepository
                    .existsByLimitLimitIdAndBreachDate(result.getLimitId(), today);

            if (alreadyOpen) {
                log.debug("LimitBreachPersistence: breach already recorded today for limitId={}, skipping insert",
                        result.getLimitId());
                continue;
            }

            LimitBreach breach = LimitBreach.builder()
                    .breachId(nextBreachId++)
                    .limit(limit)
                    .portfolio(portfolio)
                    .breachDate(today)
                    .limitValue(result.getLimitValue())
                    .actualValue(result.getActualValue())
                    .excessAmount(result.getExcessAmount())
                    .severity(result.getSeverity())
                    .status(LimitBreachStatus.OPEN)
                    .build();

            limitBreachRepository.save(breach);
            newBreaches++;

            log.info("LimitBreachPersistence: new breach recorded — breachId={} limitId={} type={} severity={}",
                    breach.getBreachId(), result.getLimitId(), result.getLimitType(), result.getSeverity());
        }

        return newBreaches;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Updates {@code current_value}, {@code utilisation_pct}, and {@code status}
     * on the {@link Limit} entity so the database always reflects the latest
     * computed values.
     */
    private void updateLimitRecord(Limit limit, LimitCheckResultDTO result) {
        limit.setCurrentValue(result.getActualValue());
        limit.setUtilisationPct(result.getUtilisationPct());

        LimitStatus newStatus;
        if (result.isBreached()) {
            newStatus = LimitStatus.BREACH;
        } else if (result.isWarning()) {
            newStatus = LimitStatus.WARNING;
        } else {
            newStatus = LimitStatus.OK;
        }

        limit.setStatus(newStatus);
        limitRepository.save(limit);
    }
}

