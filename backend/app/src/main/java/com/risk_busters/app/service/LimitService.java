package com.risk_busters.app.service;

import com.risk_busters.app.dto.*;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitBreach;
import com.risk_busters.app.model.LimitBreachStatus;
import com.risk_busters.app.repository.LimitBreachRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public LimitBreachResponseDTO getLimitBreachesByStatus(LimitBreachStatus status) {
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

        return LimitBreachResponseDTO.builder()
                .status(status)
                .limits(limitBreaches)
                .build();
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
    public AcknowledgeLimitResponseDTO acknowledgeLimitBreach(Integer limitId, AcknowledgeLimitRequestDTO request) {
        log.info("Limit breach acknowledgement started: limitId={} acknowledgedBy={}", limitId, request.getAcknowledgedBy());

        // Find the most recent open breach for this limit
        LimitBreach breach = limitBreachRepository
                .findFirstByLimitLimitIdOrderByBreachDateDesc(limitId)
                .orElseThrow(() -> {
                    log.error("Limit breach acknowledgement failed: limitId={} reason=No limit breach found", limitId);
                    return new ResourceNotFoundException("No limit breach found for limit ID: " + limitId);
                });

        // Update breach record
        //todo think about moving it to a function
        breach.setAcknowledgedBy(request.getAcknowledgedBy());
        breach.setAcknowledgedAt(LocalDateTime.now());
        breach.setResolution(request.getResolution());
        breach.setStatus(LimitBreachStatus.ACKNOWLEDGED);
        LimitBreach saved = limitBreachRepository.save(breach);


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

    private LimitDetailDTO toLimitDetailDto(LimitBreach breach) {
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
                .isBreached(breach.getStatus() != LimitBreachStatus.RESOLVED
                        && breach.getStatus() != LimitBreachStatus.WAIVED)
                .build();
    }
}
