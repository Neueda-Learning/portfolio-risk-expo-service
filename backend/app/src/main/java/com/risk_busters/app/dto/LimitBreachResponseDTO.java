package com.risk_busters.app.dto;

import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitStatus;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitBreachResponseDTO {
    private LimitStatus status;
    private List<LimitDetailDTO> limits;
}
