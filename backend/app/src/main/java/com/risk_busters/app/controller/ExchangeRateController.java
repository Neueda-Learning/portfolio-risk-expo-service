package com.risk_busters.app.controller;

import com.risk_busters.app.dto.ExchangeRateDTO;
import com.risk_busters.app.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/fx-rates")
@RequiredArgsConstructor
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;

    @GetMapping
    public ResponseEntity<List<ExchangeRateDTO>> getAllExchangeRates(){
        return ResponseEntity.ok(exchangeRateService.getAllExchangeRates());
    }
    @GetMapping("/{fromCurrencyCode}")
    public ResponseEntity<List<ExchangeRateDTO>> getExchangeRatesByFromCurrencyCode(@PathVariable String fromCurrencyCode){
        return ResponseEntity.ok(exchangeRateService.getExchangeRatesByBaseCurrencyCode(fromCurrencyCode));
    }

}
