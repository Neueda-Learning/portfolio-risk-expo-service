package com.risk_busters.app.dto;

import com.risk_busters.app.model.LimitBreachStatus;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitBreachResponseDTO {
    private LimitBreachStatus status;
    private List<LimitBreachDTO> limits;
}
