package com.risk_busters.app.mapper;

import com.risk_busters.app.dto.LimitDetailDTO;
import com.risk_busters.app.model.Limit;
public interface LimitMapper {
    LimitDetailDTO toDto(Limit limit);

    LimitDetailDTO toDto(Limit limit, java.math.BigDecimal fallbackCurrentValue);
}


