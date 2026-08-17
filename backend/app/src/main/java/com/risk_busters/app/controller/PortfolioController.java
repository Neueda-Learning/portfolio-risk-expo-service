package com.risk_busters.app.controller;

import com.risk_busters.app.dto.ExposureResponseDTO;
import com.risk_busters.app.dto.PortfolioLimitsResponseDTO;
import com.risk_busters.app.service.PortfolioRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {
    
    private final PortfolioRiskService portfolioRiskService;
    
    /**
     * GET /api/portfolios/{id}/exposure
     * Return current total exposure for a portfolio
     */
    @GetMapping("/{id}/exposure")
    public ResponseEntity<ExposureResponseDTO> getPortfolioExposure(@PathVariable Integer id) {
        ExposureResponseDTO exposure = portfolioRiskService.calculateExposure(id);
        return ResponseEntity.ok(exposure);
    }
    
    /**
     * GET /api/portfolios/{id}/limits
     * Return portfolio limits and utilisation
     */
    @GetMapping("/{id}/limits")
    public ResponseEntity<PortfolioLimitsResponseDTO> getPortfolioLimits(@PathVariable Integer id) {
        PortfolioLimitsResponseDTO limits = portfolioRiskService.getPortfolioLimits(id);
        return ResponseEntity.ok(limits);
    }
}

