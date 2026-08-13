package com.risk_busters.app.service;

import com.risk_busters.app.dto.LimitBreachResponseDTO;
import com.risk_busters.app.model.LimitStatus;
import com.risk_busters.app.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LimitService {
    private final LimitRepository limitRepository;

    public List<LimitBreachResponseDTO> getLimitBreachesByStatus(LimitStatus status) {
        return Collections.singletonList(
                LimitBreachResponseDTO.builder()
                        .status(status)
                        .limits(limitRepository.findByStatus(status))
                        .build()
        );
    }
}
