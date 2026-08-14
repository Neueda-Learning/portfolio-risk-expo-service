package com.risk_busters.app.service;

import com.risk_busters.app.dto.LimitBreachResponseDTO;
import com.risk_busters.app.dto.LimitDetailDTO;
import com.risk_busters.app.mapper.LimitMapper;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LimitService {
    private final LimitRepository limitRepository;
    private final LimitMapper limitMapper;

    public LimitBreachResponseDTO getLimitBreachesByStatus(LimitStatus status) {
        List<LimitDetailDTO> mappedLimits = limitRepository.findByStatus(status).stream()
                .map(limitMapper::toDto)
                .toList();

        return LimitBreachResponseDTO.builder()
                        .status(status)
                        .limits(mappedLimits)
                        .build();
    }
}
