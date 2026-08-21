package com.risk_busters.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"from_currency", "to_currency", "effective_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

	@Id
	@Column(name = "rate_id")
	private Integer rateId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_currency", referencedColumnName = "currency_code", nullable = false)
	private Currency fromCurrency;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_currency", referencedColumnName = "currency_code", nullable = false)
	private Currency toCurrency;

	@Column(nullable = false, precision = 12, scale = 6)
	private BigDecimal rate;

	@Column(name = "effective_date", nullable = false)
	private LocalDate effectiveDate;

	@Column(length = 50)
	private String source;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}
