package com.risk_busters.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcknowledgeLimitRequestDTO {
    private String acknowledgedBy;
    private String resolution;
}

