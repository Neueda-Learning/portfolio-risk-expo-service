package com.risk_busters.app.controller;

import com.risk_busters.app.dto.*;
import com.risk_busters.app.service.PortfolioRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * GET /api/portfolios/{id}/exposure/by-sector
     * Return current exposure for a portfolio by sector
     */
    @GetMapping("/{id}/exposure/by-sector")
    public ResponseEntity<SectorExposureResponseDTO> getPortfolioExposureBySector(@PathVariable Integer id) {
        SectorExposureResponseDTO sectorExposure = portfolioRiskService.calculateExposureBySector(id);
        return ResponseEntity.ok(sectorExposure);
    }
    /**
     * GET /api/portfolios/{id}/exposure/by-asset
     * Return current exposure for a portfolio by asset
     */
    @GetMapping("/{id}/exposure/by-asset")
    public ResponseEntity<AssetExposureResponseDTO> getPortfolioExposureByAsset(@PathVariable Integer id) {
        AssetExposureResponseDTO assetExposure = portfolioRiskService.calculateExposureByAsset(id);
        return ResponseEntity.ok(assetExposure);
    }
    /**
     * GET /api/portfolios/{id}/var?confidence=95
     * Calculating 1-day VaR with set confidence score
     */
    @GetMapping("/{id}/var")
    public ResponseEntity<VarResponseDTO> getPortfolioVarConfidence(@PathVariable Integer id, @RequestParam(name = "confidence", defaultValue = "95") Integer confidence) {
        VarResponseDTO varResponse = portfolioRiskService.calculate1DayVar(id, confidence);
        return ResponseEntity.ok(varResponse);
    }
}

