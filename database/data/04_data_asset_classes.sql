-- Graduate Programme 2026 — Project 03 · Portfolio Risk & Exposure
-- Asset Class Data
-- Generated: 2026-03-26 19:51:57
--
-- Consistent with DDL schema and FK requirements.
-- Load order: 00_ddl_schema.sql → 01_data_portfolios.sql → ... → 09_data_asset_classes.sql

INSERT INTO asset_class (asset_class_id, asset_class_code, asset_class_name, description, is_active) VALUES
(1, 'EQUITY', 'Equity', 'Equity securities including stocks and equity derivatives', TRUE),
(2, 'FIXED_INCOME', 'Fixed Income', 'Bonds, notes, and fixed income securities', TRUE),
(3, 'FX', 'Foreign Exchange', 'Currency spot, forwards, and FX derivatives', TRUE),
(4, 'DERIVATIVE', 'Derivatives', 'Options, futures, swaps, and other derivative instruments', TRUE),
(5, 'COMMODITY', 'Commodities', 'Commodity spot prices and commodity futures', TRUE);
