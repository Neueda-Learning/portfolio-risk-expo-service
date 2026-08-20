package com.risk_busters.app.dto;

import com.risk_busters.app.model.AssetClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstrumentDTO {
    private Integer instrumentId;
    private String instrumentIsin;
    private String instrumentName;
    private String currency;
    private LocalDate issueDate;
    private LocalDate maturityDate;
    private String issuer;
    private String sector;
    private String assetClass;
    private String assetClassId;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
