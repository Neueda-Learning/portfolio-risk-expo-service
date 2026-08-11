-- Graduate Programme 2026 — Project 03 · Portfolio Risk & Exposure
-- Risk Factor Data
-- Generated: 2026-03-26 19:51:57
--
-- Consistent with DDL schema and FK requirements.
-- Load order: 00_ddl_schema.sql → 01_data_portfolios.sql → ... → 08_data_limit_breaches.sql

INSERT INTO risk_factor (factor_id, factor_code, factor_name, factor_type, currency, current_value, previous_value, change_pct, as_of_date) VALUES
(1, 'FTSE100', 'FTSE 100 Index', 'EQUITY_INDEX', 'GBP', 7825.50, 7750.25, 0.97, '2026-03-20'),
(2, 'SPX', 'S&P 500 Index', 'EQUITY_INDEX', 'USD', 5298.75, 5210.50, 1.69, '2026-03-20'),
(3, 'STOXX600', 'STOXX 600 Index', 'EQUITY_INDEX', 'EUR', 502.40, 495.30, 1.43, '2026-03-20'),
(4, 'MSCIAP', 'MSCI Asia-Pacific ex-Japan', 'EQUITY_INDEX', 'USD', 654.20, 641.80, 1.92, '2026-03-20'),
(5, 'MSCIEMRG', 'MSCI Emerging Markets', 'EQUITY_INDEX', 'USD', 1142.50, 1115.30, 2.44, '2026-03-20'),
(6, 'GC10Y', 'UK Gilt 10Y Yield', 'INTEREST_RATE', 'GBP', 3.85, 3.92, -1.79, '2026-03-20'),
(7, 'US10Y', 'US Treasury 10Y Yield', 'INTEREST_RATE', 'USD', 4.25, 4.10, 3.66, '2026-03-20'),
(8, 'EUR10Y', 'German Bund 10Y Yield', 'INTEREST_RATE', 'EUR', 2.50, 2.45, 2.04, '2026-03-20'),
(9, 'ITRXEUR', 'iTraxx Europe CDS Index', 'CREDIT_SPREAD', 'EUR', 72.50, 68.30, 6.15, '2026-03-20'),
(10, 'ITRXXHVOL', 'iTraxx Crossover HY Spread', 'CREDIT_SPREAD', 'EUR', 185.25, 172.50, 7.41, '2026-03-20'),
(11, 'EURUSD', 'EUR/USD Exchange Rate', 'FX', 'USD', 1.0850, 1.0725, 1.16, '2026-03-20'),
(12, 'GBPUSD', 'GBP/USD Exchange Rate', 'FX', 'USD', 1.2650, 1.2480, 1.36, '2026-03-20'),
(13, 'VIX', 'CBOE Volatility Index', 'VOLATILITY', 'USD', 18.50, 16.20, 14.20, '2026-03-20'),
(14, 'VSTOXX', 'EURO STOXX 50 Volatility', 'VOLATILITY', 'EUR', 18.75, 17.30, 8.36, '2026-03-20'),
(15, 'GOLDSPOT', 'Gold Spot Price', 'COMMODITY', 'USD', 2050.50, 2015.75, 1.72, '2026-03-20'),
(16, 'CRUDEOIL', 'Crude Oil WTI Spot', 'COMMODITY', 'USD', 82.45, 79.50, 3.71, '2026-03-20'),
(17, 'COPPER', 'Copper Spot Price', 'COMMODITY', 'USD', 9450.25, 9180.50, 2.93, '2026-03-20'),
(18, 'NATURAL', 'Natural Gas Spot', 'COMMODITY', 'USD', 2.85, 3.10, -8.06, '2026-03-20'),
(19, 'JPYUSD', 'JPY/USD Exchange Rate', 'FX', 'USD', 150.50, 148.20, 1.55, '2026-03-20'),
(20, 'AUDUSD', 'AUD/USD Exchange Rate', 'FX', 'USD', 0.6575, 0.6450, 1.94, '2026-03-20');
