package com.risk_busters.app.controller;

import com.risk_busters.app.dto.*;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.model.LimitBreachStatus;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.repository.PortfolioRepository;
import com.risk_busters.app.service.LimitBreachPersistenceService;
import com.risk_busters.app.service.LimitComparisonService;
import com.risk_busters.app.service.LimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/limits")
@RequiredArgsConstructor
public class LimitController {
    private final LimitService limitService;
    private final LimitComparisonService limitComparisonService;
    private final LimitBreachPersistenceService limitBreachPersistenceService;
    private final PortfolioRepository portfolioRepository;

    @GetMapping("/breaches")
    public ResponseEntity<List<LimitBreachDTO>> getLimitBreaches(
            @RequestParam(name = "status", defaultValue = "OPEN") LimitBreachStatus status) {
        List<LimitBreachDTO> breaches = limitService.getLimitBreachesByStatus(status);
        return ResponseEntity.ok(breaches);
    }

    @PatchMapping("/breaches/{id}/acknowledge")
    public ResponseEntity<AcknowledgeLimitResponseDTO> acknowledgeLimitResponse(
            @PathVariable Integer id,
            @RequestBody AcknowledgeLimitRequestDTO request) {
        AcknowledgeLimitResponseDTO acknowledgment = limitService.acknowledgeLimitBreach(id, request);
        return ResponseEntity.ok(acknowledgment);
    }

    @PostMapping("/check")
    public ResponseEntity<LimitCheckResponseDTO> runAllLimitCheck(){
        Map<Integer, List<LimitCheckResultDTO>> resultsByPortfolio = limitComparisonService.compareAllLimitsInAllPortfolios();
        for (Map.Entry<Integer, List<LimitCheckResultDTO>> entry : resultsByPortfolio.entrySet()) {
            Integer portfolioId = entry.getKey();
            List<LimitCheckResultDTO> results = entry.getValue();
            if (results != null && !results.isEmpty()) {
                limitBreachPersistenceService.persistBreaches(portfolioId, results);
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check/{portfolioId}")
    public ResponseEntity<LimitCheckResponseDTO> runLimitCheck(@PathVariable Integer portfolioId) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        List<LimitCheckResultDTO> results = limitComparisonService.compareAllLimitsInPortfolio(portfolioId);

        int newBreachesRecorded = limitBreachPersistenceService.persistBreaches(portfolioId, results);

        int breachedCount = (int) results.stream().filter(LimitCheckResultDTO::isBreached).count();
        int warningCount  = (int) results.stream().filter(LimitCheckResultDTO::isWarning).count();
        int skippedCount  = (int) results.stream().filter(LimitCheckResultDTO::isSkipped).count();

        LimitCheckResponseDTO response = LimitCheckResponseDTO.builder()
                .portfolioId(portfolioId)
                .portfolioName(portfolio.getPortfolioName())
                .checkedAt(LocalDateTime.now())
                .totalLimits(results.size())
                .breachedCount(breachedCount)
                .warningCount(warningCount)
                .skippedCount(skippedCount)
                .newBreachesRecorded(newBreachesRecorded)
                .results(results)
                .build();

        return ResponseEntity.ok(response);
    }
}
