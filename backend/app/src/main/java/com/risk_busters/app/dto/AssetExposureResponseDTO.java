package com.risk_busters.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetExposureResponseDTO {
    private Integer portfolioId;
    private String portfolioName;
    private Map<String, BigDecimal> assetExposures;
}
