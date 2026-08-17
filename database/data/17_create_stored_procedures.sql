-- Graduate Programme 2026 — Project 03 · Portfolio Risk & Exposure
-- Stored Procedures Definition (PostgreSQL Version)
--
-- Database stored procedures for business logic
-- Load order: Load after all tables and data are populated

-- ============================================================================
-- STORED PROCEDURE 1: calculate_exposure
-- Purpose: Sum positions × current price per portfolio; convert to base currency using exchange rates
-- ============================================================================
CREATE OR REPLACE PROCEDURE calculate_exposure(
    p_portfolio_id INT,
    p_snapshot_date DATE DEFAULT NULL,
    INOUT p_exposure_value DECIMAL(15,2) DEFAULT 0,
    INOUT p_position_count INT DEFAULT 0,
    INOUT p_var_1day_95 DECIMAL(15,2) DEFAULT 0,
    INOUT p_largest_position_pct DECIMAL(8,4) DEFAULT 0
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_base_currency CHAR(3);
    v_num_positions INT := 0;
    v_total_market_value DECIMAL(15,2) := 0;
    v_max_weight DECIMAL(8,4) := 0;
    v_total_var DECIMAL(15,2) := 0;
BEGIN
    -- Get portfolio currency
    SELECT base_currency INTO v_base_currency
    FROM portfolio
    WHERE portfolio_id = p_portfolio_id;

    IF v_base_currency IS NULL THEN
        RAISE EXCEPTION 'Portfolio not found';
    END IF;

    -- Set snapshot date to today if not provided
    IF p_snapshot_date IS NULL THEN
        p_snapshot_date := CURRENT_DATE;
    END IF;

    -- Calculate total exposure with FX conversion to base currency
    SELECT
        COALESCE(SUM(
            CASE
                WHEN i.currency = v_base_currency THEN pos.market_value_base
                ELSE pos.market_value * COALESCE((
                    SELECT rate
                    FROM exchange_rate
                    WHERE from_currency = i.currency
                    AND to_currency = v_base_currency
                    AND effective_date <= p_snapshot_date
                    AND is_active = TRUE
                    ORDER BY effective_date DESC
                    LIMIT 1
                ), 1.0)
            END
        ), 0),
        CAST(COUNT(DISTINCT pos.position_id) AS INT),
        COALESCE(MAX(pos.weight_pct), 0),
        COALESCE(SUM(COALESCE(rm.var_1d_95, 0)), 0)
    INTO
        v_total_market_value,
        v_num_positions,
        v_max_weight,
        v_total_var
    FROM position pos
    LEFT JOIN risk_metric rm ON pos.position_id = rm.position_id
    LEFT JOIN instrument i ON pos.instrument_id = i.instrument_id
    WHERE pos.portfolio_id = p_portfolio_id;

    -- Set output parameters
    p_exposure_value := v_total_market_value;
    p_position_count := v_num_positions;
    p_var_1day_95 := v_total_var;
    p_largest_position_pct := v_max_weight;
END;
$$;


-- ============================================================================
-- STORED PROCEDURE 2: check_limits
-- Purpose: Compare current exposures against all limits; insert LimitBreach records for any breaches
-- ============================================================================
CREATE OR REPLACE PROCEDURE check_limits(
    p_portfolio_id INT DEFAULT NULL,
    p_breach_date DATE DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_portfolio_check INT;
BEGIN
    -- Set breach date to today if not provided
    IF p_breach_date IS NULL THEN
        p_breach_date := CURRENT_DATE;
    END IF;

    -- If specific portfolio provided, check only that portfolio
    IF p_portfolio_id IS NOT NULL THEN
        SELECT COUNT(*) INTO v_portfolio_check FROM portfolio WHERE portfolio_id = p_portfolio_id;
        IF v_portfolio_check = 0 THEN
            RAISE EXCEPTION 'Portfolio not found';
        END IF;
    END IF;

    -- Update risk_limit status and check for breaches
    UPDATE risk_limit
    SET status = CASE
            WHEN current_value > limit_value THEN 'BREACH'
            WHEN current_value > warning_threshold THEN 'WARNING'
            ELSE 'OK'
        END,
        utilisation_pct = ROUND(100.0 * current_value / NULLIF(limit_value, 0), 4)
    WHERE (p_portfolio_id IS NULL OR portfolio_id = p_portfolio_id)
        AND status != 'SUSPENDED'
        AND effective_from <= p_breach_date
        AND (effective_to IS NULL OR effective_to >= p_breach_date);

    -- Insert breach records for new breaches
    INSERT INTO limit_breach (limit_id, portfolio_id, breach_date, limit_value, actual_value,
                               excess_amount, severity, status)
    SELECT
        rl.limit_id,
        rl.portfolio_id,
        p_breach_date,
        rl.limit_value,
        rl.current_value,
        rl.current_value - rl.limit_value,
        CASE
            WHEN (rl.current_value - rl.limit_value) / rl.limit_value > 0.10 THEN 'CRITICAL'
            WHEN (rl.current_value - rl.limit_value) / rl.limit_value > 0.05 THEN 'MAJOR'
            ELSE 'MINOR'
        END,
        'OPEN'
    FROM risk_limit rl
    WHERE rl.status = 'BREACH'
        AND (p_portfolio_id IS NULL OR rl.portfolio_id = p_portfolio_id)
        AND NOT EXISTS (
            SELECT 1 FROM limit_breach lb
            WHERE lb.limit_id = rl.limit_id
                AND lb.breach_date = p_breach_date
                AND lb.status = 'OPEN'
        );

EXCEPTION WHEN OTHERS THEN
    -- PL/pgSQL automatically rolls back the transaction block on an exception
    RAISE EXCEPTION 'Error checking limits: %', SQLERRM;
END;
$$;


-- ============================================================================
-- STORED PROCEDURE 3: store_snapshot
-- Purpose: Insert end-of-day ExposureSnapshot with VaR and concentration metrics
-- ============================================================================
CREATE OR REPLACE PROCEDURE store_snapshot(
    p_portfolio_id INT,
    p_snapshot_date DATE DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_total_exposure DECIMAL(15,2) := 0;
    v_var_1day_95 DECIMAL(15,2) := 0;
    v_var_1day_99 DECIMAL(15,2) := 0;
    v_var_10day_99 DECIMAL(15,2) := 0;
    v_largest_position_pct DECIMAL(8,4) := 0;
    v_base_currency CHAR(3);
    v_num_positions INT := 0;
    v_concentration_herfindahl DECIMAL(8,4) := 0;
    v_sum_weights_squared DECIMAL(12,6) := 0;
    v_portfolio_exists INT;
BEGIN
    IF p_snapshot_date IS NULL THEN
        p_snapshot_date := CURRENT_DATE;
    END IF;

    -- Verify portfolio exists
    SELECT COUNT(*) INTO v_portfolio_exists FROM portfolio WHERE portfolio_id = p_portfolio_id;
    IF v_portfolio_exists = 0 THEN
        RAISE EXCEPTION 'Portfolio not found';
    END IF;

    -- Get portfolio base currency
    SELECT base_currency INTO v_base_currency
    FROM portfolio
    WHERE portfolio_id = p_portfolio_id;

    -- Calculate portfolio-level exposure metrics
    SELECT
        COALESCE(SUM(pos.market_value_base), 0),
        COALESCE(SUM(rm.var_1d_95), 0),
        COALESCE(SUM(rm.var_1d_95 * 1.20), 0),  -- Approximation
        COALESCE(SUM(rm.var_10d_99), 0),
        COALESCE(MAX(pos.weight_pct), 0),
        CAST(COUNT(DISTINCT pos.position_id) AS INT),
        COALESCE(SUM(POWER(pos.weight_pct / 100.0, 2)), 0)
    INTO
        v_total_exposure,
        v_var_1day_95,
        v_var_1day_99,
        v_var_10day_99,
        v_largest_position_pct,
        v_num_positions,
        v_sum_weights_squared
    FROM position pos
    LEFT JOIN risk_metric rm ON pos.position_id = rm.position_id
    WHERE pos.portfolio_id = p_portfolio_id;

    -- Calculate Herfindahl index (concentration measure)
    v_concentration_herfindahl := ROUND(v_sum_weights_squared * 10000, 4);

    -- Check if snapshot for this date already exists
    IF EXISTS (SELECT 1 FROM exposure_snapshot WHERE portfolio_id = p_portfolio_id AND snapshot_date = p_snapshot_date) THEN
        UPDATE exposure_snapshot
        SET total_exposure = v_total_exposure,
            var_1day_95 = v_var_1day_95,
            var_1day_99 = v_var_1day_99,
            var_10day_99 = v_var_10day_99,
            largest_position_pct = v_largest_position_pct,
            num_positions = v_num_positions,
            concentration_herfindahl = v_concentration_herfindahl
        WHERE portfolio_id = p_portfolio_id AND snapshot_date = p_snapshot_date;
    ELSE
        INSERT INTO exposure_snapshot
            (portfolio_id, snapshot_date, total_exposure, var_1day_95, var_1day_99,
             var_10day_99, largest_position_pct, currency, num_positions, concentration_herfindahl)
        VALUES
            (p_portfolio_id, p_snapshot_date, v_total_exposure, v_var_1day_95, v_var_1day_99,
             v_var_10day_99, v_largest_position_pct, v_base_currency, v_num_positions, v_concentration_herfindahl);
    END IF;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'Error storing snapshot: %', SQLERRM;
END;
$$;


-- ============================================================================
-- STORED PROCEDURE 4: update_risk_metrics
-- Purpose: Update risk metrics for all positions in a portfolio
-- ============================================================================
CREATE OR REPLACE PROCEDURE update_risk_metrics(
    p_portfolio_id INT,
    p_metric_date DATE DEFAULT NULL,
    p_var_1d_95_multiplier DECIMAL(5,2) DEFAULT 1.0
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_metric_date IS NULL THEN
        p_metric_date := CURRENT_DATE;
    END IF;

    -- Update risk metrics for positions
    -- Note: PostgreSQL uses a different UPDATE FROM syntax than T-SQL
    UPDATE risk_metric rm
    SET var_1d_95 = ROUND(rm.var_1d_95 * p_var_1d_95_multiplier, 2),
        var_10d_99 = ROUND(rm.var_10d_99 * p_var_1d_95_multiplier, 2)
    FROM position pos
    WHERE rm.position_id = pos.position_id
        AND pos.portfolio_id = p_portfolio_id
        AND rm.metric_date = p_metric_date;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'Error updating risk metrics: %', SQLERRM;
END;
$$;


-- ============================================================================
-- FUNCTION 5: get_portfolio_summary (Converted from Procedure)
-- Purpose: Get comprehensive portfolio summary with exposures and risks
-- Note: In Postgres, returning a result set is done via a FUNCTION, not a PROCEDURE
-- ============================================================================
CREATE OR REPLACE FUNCTION get_portfolio_summary(
    p_portfolio_id INT DEFAULT NULL
)
RETURNS TABLE (
    portfolio_id INT,
    portfolio_code VARCHAR,
    portfolio_name VARCHAR,
    portfolio_type VARCHAR,
    base_currency CHAR(3),
    aum DECIMAL,
    manager VARCHAR,
    is_active BOOLEAN,
    num_positions BIGINT,
    total_exposure DECIMAL,
    largest_position_pct DECIMAL,
    var_1day_95 DECIMAL,
    var_1day_99 DECIMAL,
    concentration_herfindahl DECIMAL,
    snapshot_date DATE,
    limits_breached BIGINT,
    limits_warning BIGINT,
    open_breaches BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.portfolio_id,
        p.portfolio_code,
        p.portfolio_name,
        p.portfolio_type,
        p.base_currency,
        p.aum,
        p.manager,
        p.is_active,
        COUNT(DISTINCT pos.position_id) as num_positions,
        SUM(pos.market_value_base) as total_exposure,
        ROUND(MAX(pos.weight_pct), 2) as largest_position_pct,
        es.var_1day_95,
        es.var_1day_99,
        es.concentration_herfindahl,
        es.snapshot_date,
        COUNT(DISTINCT CASE WHEN rl.status = 'BREACH' THEN rl.limit_id END) as limits_breached,
        COUNT(DISTINCT CASE WHEN rl.status = 'WARNING' THEN rl.limit_id END) as limits_warning,
        COUNT(DISTINCT lb.breach_id) as open_breaches
    FROM portfolio p
    LEFT JOIN position pos ON p.portfolio_id = pos.portfolio_id
    LEFT JOIN exposure_snapshot es ON p.portfolio_id = es.portfolio_id
        AND es.snapshot_date = (SELECT MAX(snap.snapshot_date) FROM exposure_snapshot snap WHERE snap.portfolio_id = p.portfolio_id)
    LEFT JOIN risk_limit rl ON p.portfolio_id = rl.portfolio_id
    LEFT JOIN limit_breach lb ON rl.limit_id = lb.limit_id AND lb.status = 'OPEN'
    WHERE p_portfolio_id IS NULL OR p.portfolio_id = p_portfolio_id
    GROUP BY
        p.portfolio_id, p.portfolio_code, p.portfolio_name, p.portfolio_type,
        p.base_currency, p.aum, p.manager, p.is_active,
        es.var_1day_95, es.var_1day_99, es.concentration_herfindahl, es.snapshot_date
    ORDER BY p.portfolio_id;
END;
$$;