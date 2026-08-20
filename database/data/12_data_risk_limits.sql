-- Graduate Programme 2026 — Project 03 · Portfolio Risk & Exposure
-- Risk Limit Data
-- Generated: 2026-03-26 19:51:57
--
-- Consistent with DDL schema and FK requirements.
-- Load order: 00_ddl_schema.sql → 01_data_portfolios.sql → ... → 08_data_limit_breaches.sql

INSERT INTO risk_limit (limit_id, portfolio_id, limit_type, limit_metric, limit_value, warning_threshold, current_value, utilisation_pct, status, effective_from, effective_to) VALUES
(1, 1, 'VAR', 'Daily VaR (95%)', 6000000.00, 4500000.00, 5850000.00, 97.50, 'WARNING', '2025-01-01', '2026-12-31'),
(2, 1, 'CONCENTRATION', 'Single Position', 300000000.00, 250000000.00, 204080000.00, 68.03, 'OK', '2025-01-01', '2026-12-31'),
(3, 3, 'VAR', 'Daily VaR (95%)', 8000000.00, 6000000.00, 7500000.00, 93.75, 'WARNING', '2025-01-01', '2026-12-31'),
(4, 3, 'DURATION', 'Effective Duration', 5.50, 5.00, 5.62, 102.18, 'OK', '2025-01-01', '2026-12-31'),
(5, 5, 'LEVERAGE', 'Gross Leverage Ratio', 2.00, 1.75, 1.90, 95.50, 'WARNING', '2025-01-01', '2026-12-31'),
(6, 5, 'DRAWDOWN', 'Max Drawdown YTD', -15.00, -12.00, -8.25, 55.00, 'OK', '2025-01-01', '2026-12-31'),
(7, 8, 'VAR', 'Daily VaR (95%)', 5500000.00, 4000000.00, 5200000.00, 94.55, 'OK', '2025-01-01', '2026-12-31'),
(8, 10, 'VAR', 'Daily VaR (95%)', 2000000.00, 1500000.00, 2100000.00, 105.00, 'BREACH', '2025-01-01', '2026-12-31'),
(9, 10, 'SECTOR_EXPOSURE', 'Financials Sector', 35.00, 30.00, 28.50, 81.43, 'OK', '2025-01-01', '2026-12-31'),
(10, 12, 'CONCENTRATION', 'Top 10 Positions', 60.00, 55.00, 56.00, 93.33, 'WARNING', '2025-01-01', '2026-12-31');
