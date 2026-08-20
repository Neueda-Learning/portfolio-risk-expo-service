package com.risk_busters.app.service;

import com.risk_busters.app.dto.*;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.mapper.LimitMapper;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitBreach;
import com.risk_busters.app.model.LimitBreachStatus;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.repository.LimitBreachRepository;
import com.risk_busters.app.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LimitService {
    private final LimitBreachRepository limitBreachRepository;
    private final LimitRepository limitRepository;
    private final LimitMapper limitMapper;

    public List<LimitBreachDTO> getLimitBreachesByStatus(LimitBreachStatus status) {
        log.info("Limit breaches lookup started: status={}", status);

        //This maps breaches to Limits, not breaches themselves - might be used somewhere else
//        List<LimitDetailDTO> mappedLimits = limitBreachRepository.findByStatus(status).stream()
//                .map(this::toLimitDetailDto)//todo toLimitDetailDTO should go as its own mapper
//                .toList();

        List<LimitBreachDTO> limitBreaches = limitBreachRepository.findByStatus(status).stream()
                .map(this::toLimitBreachDto)
                .toList();

        if (limitBreaches.isEmpty()) {
            log.warn("No limit breaches found: status={}", status);
        } else {
            log.info("Limit breaches lookup completed: status={} count={}", status, limitBreaches.size());
        }

        return limitBreaches;
    }

    private LimitBreachDTO toLimitBreachDto(LimitBreach limitBreach) {
        return LimitBreachDTO.builder()
                .breachId(limitBreach.getBreachId())
                .limitId(limitBreach.getLimit().getLimitId())
                .portfolioId(limitBreach.getPortfolio().getPortfolioId())
                .portfolioName(limitBreach.getPortfolio().getPortfolioName())
                .breachDate(limitBreach.getBreachDate())
                .limitValue(limitBreach.getLimitValue())
                .actualValue(limitBreach.getActualValue())
                .excessAmount(limitBreach.getExcessAmount())
                .severity(limitBreach.getSeverity())
                .acknowledgedBy(limitBreach.getAcknowledgedBy())
                .acknowledgedAt(limitBreach.getAcknowledgedAt())
                .resolution(limitBreach.getResolution())
                .status(limitBreach.getStatus())
                .build();
    }

    @Transactional
    public AcknowledgeLimitResponseDTO acknowledgeLatestLimitBreachInLimit(Integer limitId, AcknowledgeLimitRequestDTO request) {
        log.info("Limit breach (latest) acknowledgement started: limitId={} acknowledgedBy={}", limitId, request.getAcknowledgedBy());

        // Find the most recent open breach for this limit
        LimitBreach breach = limitBreachRepository
                .findFirstByLimitLimitIdAndStatusOrderByBreachDateDesc(limitId, LimitBreachStatus.OPEN)
                .orElseThrow(() -> {
                    log.error("Limit breach acknowledgement failed: limitId={} reason=No limit breach found", limitId);
                    return new ResourceNotFoundException("No limit breach found for limit ID: " + limitId);
                });

        // Update breach record
        LimitBreach saved = updateBreachStatus(breach, request);
        
        // Check if there are any remaining OPEN breaches for this limit; if not, update limit status to OK
        updateLimitStatusIfNoOpenBreaches(limitId);

        //todo  eventualy move to its own mapper
        log.info("Limit breach acknowledged: limitId={} acknowledgedBy={} status={} resolution={}",
                limitId,
                saved.getAcknowledgedBy(),
                saved.getStatus(),
                saved.getResolution());

        return AcknowledgeLimitResponseDTO.builder()
                .limitId(limitId)
                .acknowledgedBy(saved.getAcknowledgedBy())
                .acknowledgedAt(saved.getAcknowledgedAt())
                .resolution(saved.getResolution())
                .status(saved.getStatus())
                .build();
    }

    private LimitDetailDTO breachToLimitDetailDto(LimitBreach breach) {
        Limit limit = breach.getLimit();

        return LimitDetailDTO.builder()
                .limitId(limit.getLimitId())
                .limitType(limit.getLimitType() == null ? null : limit.getLimitType().name())
                .limitMetric(limit.getLimitMetric())
                .limitValue(breach.getLimitValue())
                .warningThreshold(limit.getWarningThreshold())
                .currentValue(breach.getActualValue())
                .utilisationPct(limit.getUtilisationPct())
                .status(breach.getStatus().name())
                .effectiveFrom(limit.getEffectiveFrom())
                .effectiveTo(limit.getEffectiveTo())
                .isBreached(limit.getStatus() == LimitStatus.BREACH)
                .build();
    }

    public @Nullable List<LimitDetailDTO> getAllLimits() {
        log.info("Limits lookup started");
        List<LimitDetailDTO> limits = limitRepository.findAll()
                .stream().map(limitMapper::toDto).toList();
        if (limits.isEmpty()){
            log.warn("No limits found");
        }
        return limits;
    }

    @Transactional
    public AcknowledgeLimitResponseDTO acknowledgeLimitBreachById(Integer breachId, AcknowledgeLimitRequestDTO request) {
        log.info("Limit breach acknowledgement started: breachId={} acknowledgedBy={}", breachId, request.getAcknowledgedBy());

        // Find the breach by ID
        LimitBreach breach = limitBreachRepository.findById(breachId)
                .orElseThrow(() -> {
                    log.error("Limit breach acknowledgement failed: breachId={} reason=No limit breach found", breachId);
                    return new ResourceNotFoundException("No limit breach found for breach ID: " + breachId);
                });

        // Update breach record
        LimitBreach saved = updateBreachStatus(breach, request);
        Integer limitId = saved.getLimit().getLimitId();

        // Check if there are any remaining OPEN breaches for this limit; if not, update limit status to OK
        updateLimitStatusIfNoOpenBreaches(limitId);

        log.info("Limit breach acknowledged: breachId={} limitId={} acknowledgedBy={} status={} resolution={}",
                breachId,
                limitId,
                saved.getAcknowledgedBy(),
                saved.getStatus(),
                saved.getResolution());

        return AcknowledgeLimitResponseDTO.builder()
                .limitId(limitId)
                .acknowledgedBy(saved.getAcknowledgedBy())
                .acknowledgedAt(saved.getAcknowledgedAt())
                .resolution(saved.getResolution())
                .status(saved.getStatus())
                .build();
    }

    private LimitBreach updateBreachStatus(LimitBreach breach, AcknowledgeLimitRequestDTO request) {
        breach.setAcknowledgedBy(request.getAcknowledgedBy());
        breach.setAcknowledgedAt(LocalDateTime.now());
        breach.setResolution(request.getResolution());
        breach.setStatus(LimitBreachStatus.ACKNOWLEDGED);
        return limitBreachRepository.save(breach);
    }

    private void updateLimitStatusIfNoOpenBreaches(Integer limitId) {
        // Check if any OPEN breaches exist for this limit
        if (!limitBreachRepository.existsByLimitLimitIdAndStatus(limitId, LimitBreachStatus.OPEN)) {
            // No OPEN breaches found, update limit status to OK
            Limit limit = limitRepository.findById(limitId)
                    .orElseThrow(() -> {
                        log.error("Limit not found: limitId={}", limitId);
                        return new ResourceNotFoundException("Limit not found for limit ID: " + limitId);
                    });

            LimitStatus previousStatus = limit.getStatus();
            limit.setStatus(LimitStatus.SUSPENDED);
            limitRepository.save(limit);

            log.info("Limit status updated to SUSPENDED (ACKNOWLEDGED ALL BREACHES): limitId={} previousStatus={} newStatus={}",
                    limitId, previousStatus, LimitStatus.SUSPENDED);
        }
    }
}
