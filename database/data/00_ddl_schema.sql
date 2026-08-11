-- Graduate Programme 2026 — Project 03 · Portfolio Risk & Exposure
-- DDL Schema Definition
-- Generated: 2026-03-26 19:51:57
--
-- Consistent with DDL schema and FK requirements.
-- Load order: 00_ddl_schema.sql → 01_data_portfolios.sql → ... → 08_data_limit_breaches.sql


CREATE TABLE portfolio (
    portfolio_id INTEGER PRIMARY KEY,
    portfolio_code VARCHAR(20) NOT NULL UNIQUE,
    portfolio_name VARCHAR(100) NOT NULL,
    portfolio_type VARCHAR(20) NOT NULL CHECK (portfolio_type IN ('EQUITY','FIXED_INCOME','MULTI_ASSET','HEDGE','MONEY_MKT')),
    base_currency CHAR(3) NOT NULL,
    aum DECIMAL(15,2) NOT NULL,
    benchmark VARCHAR(50),
    risk_mandate VARCHAR(200),
    manager VARCHAR(60),
    is_active BOOLEAN NOT NULL
);

CREATE TABLE position (
    position_id INTEGER PRIMARY KEY,
    portfolio_id INTEGER NOT NULL,
    instrument_isin CHAR(12) NOT NULL,
    instrument_name VARCHAR(100) NOT NULL,
    asset_class VARCHAR(20) NOT NULL CHECK (asset_class IN ('EQUITY','FIXED_INCOME','FX','DERIVATIVE','COMMODITY')),
    position_date DATE NOT NULL,
    quantity DECIMAL(18,2) NOT NULL,
    market_price DECIMAL(15,4) NOT NULL,
    market_value DECIMAL(15,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    market_value_base DECIMAL(15,2) NOT NULL,
    weight_pct DECIMAL(8,4) NOT NULL,
    cost_basis DECIMAL(15,2),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id)
);

CREATE TABLE risk_factor (
    factor_id INTEGER PRIMARY KEY,
    factor_code VARCHAR(20) NOT NULL UNIQUE,
    factor_name VARCHAR(100) NOT NULL,
    factor_type VARCHAR(20) NOT NULL CHECK (factor_type IN ('EQUITY_INDEX','INTEREST_RATE','CREDIT_SPREAD','FX','VOLATILITY','COMMODITY')),
    currency CHAR(3),
    current_value DECIMAL(15,4) NOT NULL,
    previous_value DECIMAL(15,4),
    change_pct DECIMAL(8,4),
    as_of_date DATE NOT NULL
);

CREATE TABLE risk_metric (
    metric_id INTEGER PRIMARY KEY,
    position_id INTEGER NOT NULL,
    metric_date DATE NOT NULL,
    var_1d_95 DECIMAL(15,2),
    var_10d_99 DECIMAL(15,2),
    pv01 DECIMAL(12,2),
    cs01 DECIMAL(12,2),
    delta DECIMAL(8,4),
    gamma DECIMAL(10,6),
    vega DECIMAL(12,2),
    expected_shortfall DECIMAL(15,2),
    methodology VARCHAR(30) NOT NULL CHECK (methodology IN ('HISTORICAL','MONTE_CARLO','PARAMETRIC')),
    FOREIGN KEY (position_id) REFERENCES position(position_id)
);

CREATE TABLE stress_scenario (
    scenario_id INTEGER PRIMARY KEY,
    scenario_code VARCHAR(30) NOT NULL UNIQUE,
    scenario_name VARCHAR(100) NOT NULL,
    scenario_type VARCHAR(20) NOT NULL CHECK (scenario_type IN ('HISTORICAL','HYPOTHETICAL','REGULATORY')),
    description VARCHAR(300),
    equity_shock_pct DECIMAL(8,2),
    rate_shock_bps INTEGER,
    credit_spread_shock_bps INTEGER,
    fx_shock_pct DECIMAL(8,2),
    is_active BOOLEAN NOT NULL
);

CREATE TABLE stress_result (
    result_id INTEGER PRIMARY KEY,
    portfolio_id INTEGER NOT NULL,
    scenario_id INTEGER NOT NULL,
    result_date DATE NOT NULL,
    pnl_impact DECIMAL(15,2),
    pnl_impact_pct DECIMAL(8,4),
    var_pre DECIMAL(15,2),
    var_post DECIMAL(15,2),
    worst_position_isin CHAR(12),
    worst_position_impact DECIMAL(15,2),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id),
    FOREIGN KEY (scenario_id) REFERENCES stress_scenario(scenario_id)
);

CREATE TABLE risk_limit (
    limit_id INTEGER PRIMARY KEY,
    portfolio_id INTEGER NOT NULL,
    limit_type VARCHAR(30) NOT NULL CHECK (limit_type IN ('VAR','CONCENTRATION','DURATION','LEVERAGE','DRAWDOWN','SECTOR_EXPOSURE')),
    limit_metric VARCHAR(50),
    limit_value DECIMAL(15,4),
    warning_threshold DECIMAL(15,4),
    current_value DECIMAL(15,4),
    utilisation_pct DECIMAL(8,4),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OK','WARNING','BREACH','SUSPENDED')),
    effective_from DATE,
    effective_to DATE,
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id)
);

CREATE TABLE limit_breach (
    breach_id INTEGER PRIMARY KEY,
    limit_id INTEGER NOT NULL,
    portfolio_id INTEGER NOT NULL,
    breach_date DATE NOT NULL,
    limit_value DECIMAL(15,4),
    actual_value DECIMAL(15,4),
    excess_amount DECIMAL(15,4),
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('MINOR','MAJOR','CRITICAL')),
    acknowledged_by VARCHAR(60),
    acknowledged_at TIMESTAMP,
    resolution VARCHAR(300),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN','ACKNOWLEDGED','RESOLVED','WAIVED')),
    FOREIGN KEY (limit_id) REFERENCES risk_limit(limit_id),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id)
);
