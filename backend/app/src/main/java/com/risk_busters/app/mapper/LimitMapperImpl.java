package com.risk_busters.app.mapper;

import com.risk_busters.app.dto.LimitDetailDTO;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class LimitMapperImpl implements LimitMapper {

    @Override
    public LimitDetailDTO toDto(Limit limit) {
        return toDto(limit, null);
    }

    @Override
    public LimitDetailDTO toDto(Limit limit, BigDecimal fallbackCurrentValue) {
        if (limit == null) {
            return null;
        }

        BigDecimal currentValue = limit.getCurrentValue() != null ? limit.getCurrentValue() : fallbackCurrentValue;
        BigDecimal utilisationPct = limit.getUtilisationPct();

        if (utilisationPct == null
                && currentValue != null
                && limit.getLimitValue() != null
                && limit.getLimitValue().compareTo(BigDecimal.ZERO) > 0) {
            utilisationPct = currentValue
                    .divide(limit.getLimitValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        return LimitDetailDTO.builder()
                .limitId(limit.getLimitId())
                .limitType(limit.getLimitType() != null ? limit.getLimitType().name() : null)
                .limitMetric(limit.getLimitMetric())
                .limitValue(limit.getLimitValue())
                .warningThreshold(limit.getWarningThreshold())
                .currentValue(currentValue)
                .utilisationPct(utilisationPct)
                .status(limit.getStatus() != null ? limit.getStatus().name() : null)
                .effectiveFrom(limit.getEffectiveFrom())
                .effectiveTo(limit.getEffectiveTo())
                .isBreached(isBreached(limit, currentValue))
                .build();
    }

    private boolean isBreached(Limit limit, BigDecimal currentValue) {
        if (LimitStatus.BREACH.equals(limit.getStatus())) {
            return true;
        }

        BigDecimal limitValue = limit.getLimitValue();
        return currentValue != null && limitValue != null && currentValue.compareTo(limitValue) > 0;
    }
}

