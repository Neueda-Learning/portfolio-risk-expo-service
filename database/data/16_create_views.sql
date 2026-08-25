-- ============================================================================
-- VIEW 1: limit_breaches_vw
-- Purpose: Open breaches joined with limit and portfolio details, ordered by breach_pct desc
-- ============================================================================
CREATE VIEW limit_breaches_vw AS
SELECT 
    lb.breach_id,
    lb.limit_id,
    lb.portfolio_id,
    p.portfolio_code,
    p.portfolio_name,
    p.manager,
    rl.limit_type,
    rl.limit_metric,
    lb.breach_date,
    lb.limit_value,
    lb.actual_value,
    lb.excess_amount,
    ROUND((lb.excess_amount / lb.limit_value * 100), 2) as breach_pct,
    lb.severity,
    lb.status,
    lb.acknowledged_by,
    lb.acknowledged_at,
    lb.resolution,
    (CURRENT_DATE - lb.breach_date) as days_open
FROM limit_breach lb
JOIN risk_limit rl ON lb.limit_id = rl.limit_id
JOIN portfolio p ON lb.portfolio_id = p.portfolio_id
WHERE lb.status = 'OPEN'
ORDER BY breach_pct DESC, breach_date DESC;

-- ============================================================================
-- VIEW 2: exposure_by_asset_class_vw
-- Purpose: Aggregated exposure per portfolio per asset class as % of total
-- ============================================================================
CREATE VIEW exposure_by_asset_class_vw AS
SELECT 
    p.portfolio_id,
    p.portfolio_code,
    p.portfolio_name,
    p.base_currency,
    ac.asset_class_id,
    ac.asset_class_name,
    COUNT(DISTINCT pos.position_id) as num_positions,
    SUM(pos.market_value_base) as total_exposure,
    ROUND(100.0 * SUM(pos.market_value_base) /
        (SELECT SUM(market_value_base) FROM position WHERE portfolio_id = p.portfolio_id), 2) as pct_of_portfolio,
    MIN(pos.position_date) as earliest_position_date,
    MAX(pos.position_date) as latest_position_date
FROM position pos
JOIN instrument i ON pos.instrument_id = i.instrument_id
JOIN asset_class ac ON i.asset_class_id = ac.asset_class_id
JOIN portfolio p ON pos.portfolio_id = p.portfolio_id
GROUP BY 
    p.portfolio_id,
    p.portfolio_code,
    p.portfolio_name,
    p.base_currency,
    ac.asset_class_id,
    ac.asset_class_name
ORDER BY p.portfolio_id, pct_of_portfolio DESC;

-- ============================================================================
-- VIEW 3: portfolio_risk_summary_vw
-- Purpose: Portfolio-level risk metrics (VAR, concentration, limits)
-- ============================================================================
CREATE VIEW portfolio_risk_summary_vw AS
SELECT 
    p.portfolio_id,
    p.portfolio_code,
    p.portfolio_name,
    p.portfolio_type,
    p.base_currency,
    p.aum,
    p.manager,
    COUNT(DISTINCT pos.position_id) as num_positions,
    SUM(pos.market_value_base) as total_exposure,
    ROUND(MAX(pos.weight_pct), 2) as largest_position_pct,
    es.var_1day_95,
    es.var_1day_99,
    es.var_10day_99,
    es.concentration_herfindahl,
    es.snapshot_date,
    COUNT(DISTINCT CASE WHEN rl.status = 'BREACH' THEN rl.limit_id END) as limits_breached,
    COUNT(DISTINCT CASE WHEN rl.status = 'WARNING' THEN rl.limit_id END) as limits_warning,
    COUNT(DISTINCT CASE WHEN rl.status = 'OK' THEN rl.limit_id END) as limits_ok
FROM portfolio p
LEFT JOIN position pos ON p.portfolio_id = pos.portfolio_id
LEFT JOIN exposure_snapshot es ON p.portfolio_id = es.portfolio_id 
    AND es.snapshot_date = (SELECT MAX(snapshot_date) FROM exposure_snapshot WHERE portfolio_id = p.portfolio_id)
LEFT JOIN risk_limit rl ON p.portfolio_id = rl.portfolio_id
GROUP BY 
    p.portfolio_id,
    p.portfolio_code,
    p.portfolio_name,
    p.portfolio_type,
    p.base_currency,
    p.aum,
    p.manager,
    es.var_1day_95,
    es.var_1day_99,
    es.var_10day_99,
    es.concentration_herfindahl,
    es.snapshot_date;

-- ============================================================================
-- VIEW 4: scenario_impact_analysis_vw
-- Purpose: Scenario impacts on risk factors with shock analysis
-- ============================================================================
CREATE VIEW scenario_impact_analysis_vw AS
SELECT 
    s.scenario_id,
    s.scenario_code,
    s.scenario_name,
    s.scenario_type,
    s.description,
    rf.factor_id,
    rf.factor_code,
    rf.factor_name,
    rf.factor_type,
    ac.asset_class_name,
    rf.current_value as factor_current_value,
    srf.shock_value,
    srf.shock_direction,
    CASE 
        WHEN srf.shock_direction = 'UP' THEN 
            rf.current_value * (1 + srf.shock_value / 100)
        WHEN srf.shock_direction = 'DOWN' THEN 
            rf.current_value * (1 - srf.shock_value / 100)
        ELSE rf.current_value
    END as factor_shocked_value,
    srf.description as impact_description,
    RANK() OVER (PARTITION BY s.scenario_id ORDER BY srf.shock_value DESC) as shock_rank
FROM scenario_risk_factor srf
JOIN stress_scenario s ON srf.scenario_id = s.scenario_id
JOIN risk_factor rf ON srf.factor_id = rf.factor_id
LEFT JOIN asset_class ac ON rf.asset_class_id = ac.asset_class_id
ORDER BY s.scenario_id, srf.shock_value DESC;

-- ============================================================================
-- VIEW 5: position_detail_vw
-- Purpose: Full position details with instrument and portfolio info
-- ============================================================================
CREATE VIEW position_detail_vw AS
SELECT 
    pos.position_id,
    pos.portfolio_id,
    p.portfolio_code,
    p.portfolio_name,
    p.base_currency,
    pos.instrument_id,
    i.instrument_isin,
    i.instrument_name,
    ac.asset_class_name,
    i.currency as instrument_currency,
    i.issuer,
    i.sector,
    pos.position_date,
    pos.quantity,
    pos.market_price,
    pos.market_value,
    pos.market_value_base,
    pos.weight_pct,
    pos.cost_basis,
    ROUND(100.0 * (pos.market_value_base - pos.cost_basis) / NULLIF(pos.cost_basis, 0), 2) as pnl_pct,
    ph.close_price as latest_price,
    ph.price_date as latest_price_date,
    rm.var_1d_95,
    rm.var_10d_99,
    rm.delta,
    rm.vega,
    rm.gamma,
    pos.created_at,
    pos.updated_at
FROM position pos
JOIN portfolio p ON pos.portfolio_id = p.portfolio_id
JOIN instrument i ON pos.instrument_id = i.instrument_id
JOIN asset_class ac ON i.asset_class_id = ac.asset_class_id
LEFT JOIN price_history ph ON i.instrument_id = ph.instrument_id 
    AND ph.price_date = (SELECT MAX(price_date) FROM price_history WHERE instrument_id = i.instrument_id)
LEFT JOIN risk_metric rm ON pos.position_id = rm.position_id 
    AND rm.metric_date = (SELECT MAX(metric_date) FROM risk_metric WHERE position_id = pos.position_id)
ORDER BY p.portfolio_id, pos.weight_pct DESC;

-- ============================================================================
-- VIEW 6: stress_test_results_vw
-- Purpose: Stress test results with scenario and portfolio details
-- ============================================================================
CREATE VIEW stress_test_results_vw AS
SELECT 
    sr.result_id,
    sr.portfolio_id,
    p.portfolio_code,
    p.portfolio_name,
    p.portfolio_type,
    p.base_currency,
    sr.scenario_id,
    s.scenario_code,
    s.scenario_name,
    s.scenario_type,
    sr.result_date,
    sr.pnl_impact,
    sr.pnl_impact_pct,
    sr.var_pre,
    sr.var_post,
    ROUND(sr.var_post - sr.var_pre, 2) as var_change,
    ROUND(100.0 * (sr.var_post - sr.var_pre) / NULLIF(sr.var_pre, 0), 2) as var_change_pct,
    sr.worst_position_instrument_id,
    i.instrument_isin,
    i.instrument_name,
    ac.asset_class_name,
    sr.worst_position_impact,
    RANK() OVER (PARTITION BY sr.portfolio_id ORDER BY ABS(sr.pnl_impact) DESC) as worst_scenario_rank
FROM stress_result sr
JOIN portfolio p ON sr.portfolio_id = p.portfolio_id
JOIN stress_scenario s ON sr.scenario_id = s.scenario_id
LEFT JOIN instrument i ON sr.worst_position_instrument_id = i.instrument_id
LEFT JOIN asset_class ac ON i.asset_class_id = ac.asset_class_id
ORDER BY sr.result_date DESC, ABS(sr.pnl_impact) DESC;
