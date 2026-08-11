-- Graduate Programme 2026 — Project 03 · Portfolio Risk & Exposure
-- Instrument Data
-- Generated: 2026-03-26 19:51:57
--
-- Consistent with DDL schema and FK requirements.
-- Load order: 00_ddl_schema.sql → ... → 09_data_asset_classes.sql → 10_data_instruments.sql

INSERT INTO instrument (instrument_id, instrument_isin, instrument_name, asset_class_id, currency, issue_date, maturity_date, issuer, sector, is_active) VALUES
(1, 'GB00B10RZP78', 'Unilever PLC', 1, 'GBP', NULL, NULL, 'Unilever', 'Consumer Staples', TRUE),
(2, 'GB0008325020', 'BP plc', 1, 'GBP', NULL, NULL, 'BP', 'Energy', TRUE),
(3, 'US0378691033', 'Apple Inc.', 1, 'USD', NULL, NULL, 'Apple', 'Information Technology', TRUE),
(4, 'US5949181045', 'Microsoft Corp', 1, 'USD', NULL, NULL, 'Microsoft', 'Information Technology', TRUE),
(5, 'XS2450000000', 'BNP Paribas Senior Notes', 2, 'EUR', '2020-05-15', '2030-05-15', 'BNP Paribas', 'Financials', TRUE),
(6, 'US037833AJ60', 'Apple Inc Notes 2.25%', 2, 'USD', '2018-06-01', '2028-06-01', 'Apple', 'Information Technology', TRUE),
(7, 'DE0007236101', 'Siemens AG', 1, 'EUR', NULL, NULL, 'Siemens', 'Industrials', TRUE),
(8, 'EURUSD000000', 'EUR/USD FX Spot', 3, 'USD', NULL, NULL, 'ECB/Federal Reserve', NULL, TRUE),
(9, 'XX0000000001', 'VSTOXX Volatility Future', 4, 'EUR', NULL, NULL, 'Eurex', NULL, TRUE),
(10, 'DE0001121574', 'German T-Bills 3M', 2, 'EUR', '2026-03-20', '2026-06-20', 'German Government', 'Sovereign', TRUE),
(11, 'FR0000470143', 'French T-Bills 6M', 2, 'EUR', '2026-03-20', '2026-09-20', 'French Government', 'Sovereign', TRUE),
(12, 'HK0000823308', 'AIA Group Ltd', 1, 'HKD', NULL, NULL, 'AIA', 'Financials', TRUE),
(13, 'JP3436100006', 'Toyota Motor Corp', 1, 'JPY', NULL, NULL, 'Toyota', 'Industrials', TRUE),
(14, 'GB0030045733', 'UK Gilts 4.75% 2037', 2, 'GBP', '2007-04-12', '2037-04-12', 'UK Government', 'Sovereign', TRUE),
(15, 'DE0001102382', 'German Bunds 2.0% 2041', 2, 'EUR', '2011-09-08', '2041-09-08', 'German Government', 'Sovereign', TRUE),
(16, 'GB0031862092', 'FTSE All-Share ETF', 1, 'GBP', NULL, NULL, 'iShares', NULL, TRUE),
(17, 'GBPUSD000000', 'GBP/USD FX Spot', 3, 'USD', NULL, NULL, 'Bank of England/Federal Reserve', NULL, TRUE),
(18, 'GOLDSPOT0000', 'Gold Spot Commodity', 5, 'USD', NULL, NULL, 'LBMA', NULL, TRUE);
