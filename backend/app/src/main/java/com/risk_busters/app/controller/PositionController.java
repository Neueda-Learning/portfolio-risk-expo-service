package com.risk_busters.app.controller;
import com.risk_busters.app.dto.PositionResponseDTO;
import com.risk_busters.app.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/positions")
@RequiredArgsConstructor
@Slf4j
public class PositionController {

    private final PositionService positionService;

    /**
     * GET /api/portfolios/{portfolioId}/positions/{positionId}
     * Retrieve a specific position from a portfolio
     */
    @GetMapping("/{positionId}")
    public ResponseEntity<PositionResponseDTO> getPosition(@PathVariable Integer portfolioId, @PathVariable Integer positionId) {
        log.info("Position retrieval request: portfolioId={} positionId={}", portfolioId, positionId);
        PositionResponseDTO position = positionService.getPositionByIdFromPortfolio(portfolioId, positionId);
        return ResponseEntity.ok(position);
    }

}
