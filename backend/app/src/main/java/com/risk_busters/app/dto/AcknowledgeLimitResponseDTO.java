package com.risk_busters.app.dto;

import com.risk_busters.app.model.LimitBreachStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcknowledgeLimitResponseDTO {
    private Integer limitId;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private String resolution;
    private LimitBreachStatus status;
}
