package com.risk_busters.app.service;

import com.risk_busters.app.dto.ExposureSnapshotDTO;
import com.risk_busters.app.dto.GetSnapshotResponseDTO;
import com.risk_busters.app.exceptions.ResourceNotFoundException;
import com.risk_busters.app.model.ExposureSnapshot;
import com.risk_busters.app.model.Portfolio;
import com.risk_busters.app.repository.ExposureSnapshotRepository;
import com.risk_busters.app.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioSnapshotService {

    private final PortfolioRepository portfolioRepository;
    private final ExposureSnapshotRepository exposureSnapshotRepository;

    public GetSnapshotResponseDTO getPortfolioSnapshots(Integer portfolioId, LocalDate startDate, LocalDate endDate) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required to retrieve snapshots.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate.");
        }

        List<ExposureSnapshot> snapshots = exposureSnapshotRepository.findByPortfolioPortfolioIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
                portfolioId,
                startDate,
                endDate);

        List<ExposureSnapshotDTO> snapshotDtos = new ArrayList<>();
        for (ExposureSnapshot snapshot : snapshots) {
            snapshotDtos.add(ExposureSnapshotDTO.builder()
                    .snapshotId(snapshot.getSnapshotId())
                    .snapshotDate(snapshot.getSnapshotDate())
                    .totalExposure(snapshot.getTotalExposure())
                    .var1Day95(snapshot.getVar1Day95())
                    .var1Day99(snapshot.getVar1Day99())
                    .var10Day99(snapshot.getVar10Day99())
                    .largestPositionPct(snapshot.getLargestPositionPct())
                    .currency(snapshot.getCurrency())
                    .numPositions(snapshot.getNumPositions())
                    .concentrationHerfindahl(snapshot.getConcentrationHerfindahl())
                    .build());
        }

        return GetSnapshotResponseDTO.builder()
                .portfolioId(portfolioId)
                .portfolioName(portfolio.getPortfolioName())
                .startDate(startDate)
                .endDate(endDate)
                .snapshots(snapshotDtos)
                .build();
    }
}
