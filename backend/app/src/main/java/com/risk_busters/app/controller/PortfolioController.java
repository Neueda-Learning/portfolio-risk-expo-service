package com.risk_busters.app.controller;

import com.risk_busters.app.dto.*;
import com.risk_busters.app.service.PortfolioSnapshotService;
import com.risk_busters.app.service.PortfolioRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

    private final PortfolioRiskService portfolioRiskService;
    private final PortfolioSnapshotService portfolioSnapshotService;

    @GetMapping
    public ResponseEntity <List<PortfoliosDTO>> getPortfolios(){
        log.info("Get all portfolios request received at /api/portfolios");
        List<PortfoliosDTO> allPortfolios = portfolioRiskService.getAllPortfolios();
        return ResponseEntity.ok(allPortfolios);
    }

    /**
     * GET /api/portfolios/{id}/exposure
     * Return current total exposure for a portfolio
     */
    @GetMapping("/{id}/exposure")
    public ResponseEntity<ExposureResponseDTO> getPortfolioExposure(@PathVariable Integer id) {
        log.info("Exposure request received: portfolioId={} endpoint=/api/portfolios/{id}/exposure", id);
        ExposureResponseDTO exposure = portfolioRiskService.calculateExposure(id);
        return ResponseEntity.ok(exposure);
    }

    /**
     * GET /api/portfolios/{id}/limits
     * Return portfolio limits and utilisation
     */
    @GetMapping("/{id}/limits")
    public ResponseEntity<PortfolioLimitsResponseDTO> getPortfolioLimits(@PathVariable Integer id) {
        log.info("Limits request received: portfolioId={} endpoint=/api/portfolios/{id}/limits", id);
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
    @PostMapping("/{id}/snapshots")
    public ResponseEntity<Void> storeSnapshot(@PathVariable Integer id, @RequestBody(required = false) StoreSnapshotRequestDTO request) {
        LocalDate snapshotDate = request != null ? request.getSnapshotDate() : LocalDate.now();
        portfolioSnapshotService.storeSnapshot(id, snapshotDate);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}/snapshots")
    public ResponseEntity<GetSnapshotResponseDTO> getPortfolioSnapshots(@PathVariable Integer id, @RequestParam(required = true) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
        GetSnapshotResponseDTO snapshots = portfolioSnapshotService.getPortfolioSnapshots(id, startDate, endDate);
        return ResponseEntity.ok(snapshots);
    }
}
