package com.risk_busters.app.controller;

import com.risk_busters.app.dto.LimitBreachResponseDTO;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.service.LimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/limits")
@RequiredArgsConstructor
public class LimitController {
    private final LimitService limitService;

    @GetMapping("/breaches")
    public ResponseEntity<LimitBreachResponseDTO> getLimitBreaches(@RequestParam(name="status", defaultValue = "OK") LimitStatus status) {
        LimitBreachResponseDTO breaches = limitService.getLimitBreachesByStatus(status);
        return ResponseEntity.ok(breaches);
    }
}
