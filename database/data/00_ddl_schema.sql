-- Graduate Programme 2026 — Project 03 · Portfolio Risk & Exposure
-- DDL Schema Definition
-- Generated: 2026-03-26 19:51:57
--
-- Table creation order: dependencies first (no FK constraints until referenced tables exist)
-- Load order: 00_ddl_schema.sql → 01_data_portfolios.sql → ... → 15_create_stored_procedures.sql


-- LAYER 1: Base lookup tables (no dependencies)
CREATE TABLE currency (
    currency_code CHAR(3) PRIMARY KEY,
    currency_name VARCHAR(50) NOT NULL,
    description VARCHAR(300),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

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
    is_active BOOLEAN NOT NULL,
    FOREIGN KEY (base_currency) REFERENCES currency(currency_code)
);

CREATE TABLE asset_class (
    asset_class_id INTEGER PRIMARY KEY,
    asset_class_code VARCHAR(20) NOT NULL UNIQUE,
    asset_class_name VARCHAR(100) NOT NULL,
    description VARCHAR(300),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
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

CREATE TABLE exchange_rate (
    rate_id SERIAL PRIMARY KEY,
    from_currency CHAR(3) NOT NULL,
    to_currency CHAR(3) NOT NULL,
    rate DECIMAL(12,6) NOT NULL,
    effective_date DATE NOT NULL,
    source VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(from_currency, to_currency, effective_date),
    FOREIGN KEY (from_currency) REFERENCES currency(currency_code),
    FOREIGN KEY (to_currency) REFERENCES currency(currency_code)
);

-- LAYER 2: Tables that reference LAYER 1
CREATE TABLE instrument (
    instrument_id INTEGER PRIMARY KEY,
    instrument_isin CHAR(12) NOT NULL UNIQUE,
    instrument_name VARCHAR(100) NOT NULL,
    asset_class_id INTEGER NOT NULL,
    currency CHAR(3) NOT NULL,
    issue_date DATE,
    maturity_date DATE,
    issuer VARCHAR(100),
    sector VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_class_id) REFERENCES asset_class(asset_class_id),
    FOREIGN KEY (currency) REFERENCES currency(currency_code)
);

CREATE TABLE risk_factor (
    factor_id INTEGER PRIMARY KEY,
    factor_code VARCHAR(20) NOT NULL UNIQUE,
    factor_name VARCHAR(100) NOT NULL,
    factor_type VARCHAR(20) NOT NULL CHECK (factor_type IN ('EQUITY_INDEX','INTEREST_RATE','CREDIT_SPREAD','FX','VOLATILITY','COMMODITY')),
    asset_class_id INTEGER,
    currency CHAR(3),
    current_value DECIMAL(15,4) NOT NULL,
    previous_value DECIMAL(15,4),
    change_pct DECIMAL(8,4),
    as_of_date DATE NOT NULL,
    FOREIGN KEY (asset_class_id) REFERENCES asset_class(asset_class_id),
    FOREIGN KEY (currency) REFERENCES currency(currency_code)
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

CREATE TABLE price_history (
    price_id INTEGER PRIMARY KEY,
    instrument_id INTEGER NOT NULL,
    price_date DATE NOT NULL,
    close_price DECIMAL(15,4) NOT NULL,
    open_price DECIMAL(15,4),
    high_price DECIMAL(15,4),
    low_price DECIMAL(15,4),
    volume DECIMAL(18,2),
    currency CHAR(3) NOT NULL,
    source VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(instrument_id, price_date),
    FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id)
);

CREATE TABLE exposure_snapshot (
    snapshot_id SERIAL PRIMARY KEY,
    portfolio_id INTEGER NOT NULL,
    snapshot_date DATE NOT NULL,
    total_exposure DECIMAL(15,2) NOT NULL,
    var_1day_95 DECIMAL(15,2),
    var_1day_99 DECIMAL(15,2),
    var_10day_99 DECIMAL(15,2),
    largest_position_pct DECIMAL(8,4),
    currency CHAR(3) NOT NULL,
    num_positions INTEGER,
    concentration_herfindahl DECIMAL(8,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(portfolio_id, snapshot_date),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id)
);

-- LAYER 3: Tables that reference LAYER 2
CREATE TABLE position (
    position_id INTEGER PRIMARY KEY,
    portfolio_id INTEGER NOT NULL,
    instrument_id INTEGER NOT NULL,
    position_date DATE NOT NULL,
    quantity DECIMAL(18,2) NOT NULL,
    market_price DECIMAL(15,4) NOT NULL,
    market_value DECIMAL(15,2) NOT NULL,
    market_value_base DECIMAL(15,2) NOT NULL,
    weight_pct DECIMAL(8,4) NOT NULL,
    cost_basis DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id),
    FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id)
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
    worst_position_instrument_id INTEGER,
    worst_position_impact DECIMAL(15,2),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id),
    FOREIGN KEY (scenario_id) REFERENCES stress_scenario(scenario_id),
    FOREIGN KEY (worst_position_instrument_id) REFERENCES instrument(instrument_id)
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

-- LAYER 4: Tables that reference LAYER 2 (risk_factor)
CREATE TABLE scenario_risk_factor (
    scenario_id INTEGER NOT NULL,
    factor_id INTEGER NOT NULL,
    shock_value DECIMAL(12,2) NOT NULL,
    shock_direction VARCHAR(10) NOT NULL CHECK (shock_direction IN ('UP', 'DOWN', 'NEUTRAL')),
    description VARCHAR(300),
    PRIMARY KEY (scenario_id, factor_id),
    FOREIGN KEY (scenario_id) REFERENCES stress_scenario(scenario_id),
    FOREIGN KEY (factor_id) REFERENCES risk_factor(factor_id)
);

-- LAYER 5: Tables that reference LAYER 3
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
