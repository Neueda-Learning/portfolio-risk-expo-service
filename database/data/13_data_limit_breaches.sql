-- Graduate Programme 2026 — Project 03 · Portfolio Risk & Exposure
-- Limit Breach Data
-- Generated: 2026-03-26 19:51:57
--
-- Consistent with DDL schema and FK requirements.
-- Load order: 00_ddl_schema.sql → 01_data_portfolios.sql → ... → 08_data_limit_breaches.sql

INSERT INTO limit_breach (breach_id, limit_id, portfolio_id, breach_date, limit_value, actual_value, excess_amount, severity, acknowledged_by, acknowledged_at, resolution, status) VALUES
(1, 3, 3, '2026-03-19', 8000000.00, 8750000.00, 750000.00, 'CRITICAL', 'Michael Chen', '2026-03-19 14:30:00', 'Reduced position in XS2450000000 by 10%', 'RESOLVED'),
(2, 5, 5, '2026-03-18', 2.00, 2.05, 0.05, 'MAJOR', 'Robert Taylor', '2026-03-18 16:45:00', 'Liquidated 5% of FX position', 'RESOLVED'),
(3, 8, 10, '2026-03-20', 2000000.00, 2100000.00, 100000.00, 'CRITICAL', NULL, NULL, NULL, 'OPEN'),
(4, 10, 10, '2026-03-17', 60.00, 65.25, 5.25, 'MAJOR', 'Susan Blake', '2026-03-17 11:20:00', 'Rebalanced portfolio towards underweights', 'RESOLVED'),
(5, 8, 10, '2026-03-21', 2000000.00, 2380000.00, 380000.00, 'CRITICAL', NULL, NULL, NULL, 'OPEN'),
(6, 9, 10, '2026-03-22', 35.00, 38.40, 3.40, 'MAJOR', NULL, NULL, NULL, 'OPEN'),
(7, 8, 10, '2026-03-23', 2000000.00, 2250000.00, 250000.00, 'MAJOR', NULL, NULL, NULL, 'OPEN');
