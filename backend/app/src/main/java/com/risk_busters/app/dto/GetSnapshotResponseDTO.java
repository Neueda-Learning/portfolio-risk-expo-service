package com.risk_busters.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetSnapshotResponseDTO {
	private Integer portfolioId;
	private String portfolioName;
	private LocalDate startDate;
	private LocalDate endDate;
	private List<ExposureSnapshotDTO> snapshots;
}
