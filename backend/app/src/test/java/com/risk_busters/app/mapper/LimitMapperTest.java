package com.risk_busters.app.mapper;

import com.risk_busters.app.dto.LimitDetailDTO;
import com.risk_busters.app.model.Limit;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.model.LimitType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitMapperTest {

    private final LimitMapper mapper = new LimitMapperImpl();

    @Test
    void toDtoMapsFieldsAndFlagsBreaches() {
        Limit limit = Limit.builder()
                .limitId(101)
                .limitType(LimitType.VAR)
                .limitMetric("TOTAL_EXPOSURE")
                .limitValue(new BigDecimal("1000.00"))
                .currentValue(new BigDecimal("1200.00"))
                .status(LimitStatus.WARNING)
                .build();

        LimitDetailDTO dto = mapper.toDto(limit);

        assertEquals(101, dto.getLimitId());
        assertEquals("VAR", dto.getLimitType());
        assertEquals("WARNING", dto.getStatus());
        assertTrue(dto.getIsBreached());
    }

    @Test
    void toDtoUsesFallbackCurrentValueAndCalculatesUtilisation() {
        Limit limit = Limit.builder()
                .limitId(202)
                .limitType(LimitType.VAR)
                .limitMetric("TOTAL_EXPOSURE")
                .limitValue(new BigDecimal("1000.00"))
                .currentValue(null)
                .utilisationPct(null)
                .status(LimitStatus.OK)
                .build();

        LimitDetailDTO dto = mapper.toDto(limit, new BigDecimal("800.00"));

        assertEquals(new BigDecimal("800.00"), dto.getCurrentValue());
        assertEquals(new BigDecimal("80.0000"), dto.getUtilisationPct());
    }
}



