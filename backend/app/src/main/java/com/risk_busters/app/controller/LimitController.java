package com.risk_busters.app.controller;

import com.risk_busters.app.dto.AcknowledgeLimitRequestDTO;
import com.risk_busters.app.dto.AcknowledgeLimitResponseDTO;
import com.risk_busters.app.dto.LimitBreachResponseDTO;
import com.risk_busters.app.model.LimitBreachStatus;
import com.risk_busters.app.service.LimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/limits")
@RequiredArgsConstructor
public class LimitController {
    private final LimitService limitService;

    @GetMapping("/breaches")
    public ResponseEntity<LimitBreachResponseDTO> getLimitBreaches(
            @RequestParam(name = "status", defaultValue = "OPEN") LimitBreachStatus status) {
        LimitBreachResponseDTO breaches = limitService.getLimitBreachesByStatus(status);
        return ResponseEntity.ok(breaches);
    }

    @PatchMapping("/breaches/{id}/acknowledge")
    public ResponseEntity<AcknowledgeLimitResponseDTO> acknowledgeLimitResponse(
            @PathVariable Integer id,
            @RequestBody AcknowledgeLimitRequestDTO request) {
        AcknowledgeLimitResponseDTO acknowledgment = limitService.acknowledgeLimitBreach(id, request);
        return ResponseEntity.ok(acknowledgment);
    }
}
