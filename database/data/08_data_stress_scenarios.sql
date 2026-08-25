
INSERT INTO stress_scenario (scenario_id, scenario_code, scenario_name, scenario_type, description, equity_shock_pct, rate_shock_bps, credit_spread_shock_bps, fx_shock_pct, is_active) VALUES
(1, 'GFC2008', '2008 Global Financial Crisis', 'HISTORICAL', 'Equities -50%, Credit spreads +300bps, Rates -100bps, VIX 80', -50.00, -100, 300, -5.00, TRUE),
(2, 'COVID2020', '2020 COVID-19 Pandemic', 'HISTORICAL', 'Equities -35%, Credit spreads +200bps, Rates -50bps, VIX 82', -35.00, -50, 200, -3.50, TRUE),
(3, 'BREXIT', 'Brexit Referendum Shock', 'HISTORICAL', 'GBP -10%, Equities -12%, Credit spreads +100bps, Volatility +30', -12.00, 25, 100, -10.00, TRUE),
(4, 'UKRAINE', 'Russia-Ukraine Conflict', 'HISTORICAL', 'Oil +50%, Gas +100%, Equities -15%, Volatility +40', -15.00, 50, 80, 50.00, TRUE),
(5, 'RATESHOCK', 'Hypothetical Rate Shock +200bps', 'HYPOTHETICAL', 'Yields +200bps, Bond prices -15%, Equities -8%, Spreads +50bps', -8.00, 200, 50, 2.00, TRUE);
