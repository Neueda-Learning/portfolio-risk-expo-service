-- Graduate Programme 2026 — Project 03 · Portfolio Risk & Exposure
-- Stored Procedures Definition
-- Generated: 2026-03-26 19:51:57
--
-- Database stored procedures for business logic
-- Load order: Load after all tables and data are populated

-- ============================================================================
-- STORED PROCEDURE 1: calculate_exposure
-- Purpose: Sum positions × current price per portfolio; convert to base currency using exchange rates
-- ============================================================================
CREATE PROCEDURE calculate_exposure
    @portfolio_id INT,
    @snapshot_date DATE = NULL,
    @exposure_value DECIMAL(15,2) OUTPUT,
    @position_count INT OUTPUT,
    @var_1day_95 DECIMAL(15,2) OUTPUT,
    @largest_position_pct DECIMAL(8,4) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @base_currency CHAR(3);
    DECLARE @num_positions INT = 0;
    DECLARE @total_market_value DECIMAL(15,2) = 0;
    DECLARE @max_weight DECIMAL(8,4) = 0;
    DECLARE @total_var DECIMAL(15,2) = 0;
    
    -- Get portfolio currency
    SELECT @base_currency = base_currency 
    FROM portfolio 
    WHERE portfolio_id = @portfolio_id;
    
    IF @base_currency IS NULL
    BEGIN
        RAISERROR('Portfolio not found', 16, 1);
        RETURN 1;
    END
    
    -- Set snapshot date to today if not provided
    IF @snapshot_date IS NULL
        SET @snapshot_date = CAST(GETDATE() AS DATE);
    
    -- Calculate total exposure with FX conversion to base currency
    SELECT 
        @total_market_value = SUM(
            CASE 
                WHEN i.currency = @base_currency THEN pos.market_value_base
                ELSE pos.market_value * ISNULL((
                    SELECT TOP 1 rate 
                    FROM exchange_rate 
                    WHERE from_currency = i.currency 
                    AND to_currency = @base_currency 
                    AND effective_date <= @snapshot_date
                    AND is_active = TRUE
                    ORDER BY effective_date DESC
                ), 1.0)
            END
        ),
        @num_positions = COUNT(DISTINCT pos.position_id),
        @max_weight = MAX(pos.weight_pct),
        @total_var = SUM(ISNULL(rm.var_1d_95, 0))
    FROM position pos
    LEFT JOIN risk_metric rm ON pos.position_id = rm.position_id
    LEFT JOIN instrument i ON pos.instrument_id = i.instrument_id
    WHERE pos.portfolio_id = @portfolio_id;
    
    -- Set output parameters
    SET @exposure_value = ISNULL(@total_market_value, 0);
    SET @position_count = ISNULL(@num_positions, 0);
    SET @var_1day_95 = ISNULL(@total_var, 0);
    SET @largest_position_pct = ISNULL(@max_weight, 0);
    
    -- Return success
    RETURN 0;
END;

-- ============================================================================
-- STORED PROCEDURE 2: check_limits
-- Purpose: Compare current exposures against all limits; insert LimitBreach records for any breaches
-- ============================================================================
CREATE PROCEDURE check_limits
    @portfolio_id INT = NULL,
    @breach_date DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @current_date DATE;
    DECLARE @var_limit_id INT;
    DECLARE @current_var DECIMAL(15,2);
    DECLARE @limit_value DECIMAL(15,4);
    DECLARE @excess_amount DECIMAL(15,4);
    DECLARE @severity VARCHAR(20);
    DECLARE @portfolio_check INT;
    
    -- Set breach date to today if not provided
    IF @breach_date IS NULL
        SET @breach_date = CAST(GETDATE() AS DATE);
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- If specific portfolio provided, check only that portfolio
        IF @portfolio_id IS NOT NULL
        BEGIN
            -- Verify portfolio exists
            SELECT @portfolio_check = COUNT(*) FROM portfolio WHERE portfolio_id = @portfolio_id;
            IF @portfolio_check = 0
            BEGIN
                RAISERROR('Portfolio not found', 16, 1);
                RETURN 1;
            END
        END
        
        -- Update risk_limit status and check for breaches
        UPDATE rl
        SET rl.status = CASE 
                WHEN rl.current_value > rl.limit_value THEN 'BREACH'
                WHEN rl.current_value > rl.warning_threshold THEN 'WARNING'
                ELSE 'OK'
            END,
            rl.utilisation_pct = ROUND(100.0 * rl.current_value / NULLIF(rl.limit_value, 0), 4)
        FROM risk_limit rl
        WHERE (@portfolio_id IS NULL OR rl.portfolio_id = @portfolio_id)
            AND rl.status != 'SUSPENDED'
            AND rl.effective_from <= @breach_date
            AND (rl.effective_to IS NULL OR rl.effective_to >= @breach_date);
        
        -- Insert breach records for new breaches
        INSERT INTO limit_breach (limit_id, portfolio_id, breach_date, limit_value, actual_value, 
                                   excess_amount, severity, status)
        SELECT 
            rl.limit_id,
            rl.portfolio_id,
            @breach_date,
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
            AND (@portfolio_id IS NULL OR rl.portfolio_id = @portfolio_id)
            AND NOT EXISTS (
                SELECT 1 FROM limit_breach lb 
                WHERE lb.limit_id = rl.limit_id 
                    AND lb.breach_date = @breach_date 
                    AND lb.status = 'OPEN'
            );
        
        COMMIT TRANSACTION;
        RETURN 0;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        RAISERROR('Error checking limits', 16, 1);
        RETURN 1;
    END CATCH
END;

-- ============================================================================
-- STORED PROCEDURE 3: store_snapshot
-- Purpose: Insert end-of-day ExposureSnapshot with VaR and concentration metrics
-- ============================================================================
CREATE PROCEDURE store_snapshot
    @portfolio_id INT,
    @snapshot_date DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @total_exposure DECIMAL(15,2) = 0;
    DECLARE @var_1day_95 DECIMAL(15,2) = 0;
    DECLARE @var_1day_99 DECIMAL(15,2) = 0;
    DECLARE @var_10day_99 DECIMAL(15,2) = 0;
    DECLARE @largest_position_pct DECIMAL(8,4) = 0;
    DECLARE @base_currency CHAR(3);
    DECLARE @num_positions INT = 0;
    DECLARE @concentration_herfindahl DECIMAL(8,4) = 0;
    DECLARE @sum_weights_squared DECIMAL(12,6) = 0;
    DECLARE @portfolio_exists INT;
    
    -- Set snapshot date to today if not provided
    IF @snapshot_date IS NULL
        SET @snapshot_date = CAST(GETDATE() AS DATE);
    
    -- Verify portfolio exists
    SELECT @portfolio_exists = COUNT(*) FROM portfolio WHERE portfolio_id = @portfolio_id;
    IF @portfolio_exists = 0
    BEGIN
        RAISERROR('Portfolio not found', 16, 1);
        RETURN 1;
    END
    
    -- Get portfolio base currency
    SELECT @base_currency = base_currency 
    FROM portfolio 
    WHERE portfolio_id = @portfolio_id;
    
    -- Calculate portfolio-level exposure metrics
    SELECT 
        @total_exposure = ISNULL(SUM(pos.market_value_base), 0),
        @var_1day_95 = ISNULL(SUM(rm.var_1d_95), 0),
        @var_1day_99 = ISNULL(SUM(rm.var_1d_95 * 1.20), 0),  -- Approximation
        @var_10day_99 = ISNULL(SUM(rm.var_10d_99), 0),
        @largest_position_pct = ISNULL(MAX(pos.weight_pct), 0),
        @num_positions = COUNT(DISTINCT pos.position_id),
        @sum_weights_squared = ISNULL(SUM(POWER(pos.weight_pct / 100.0, 2)), 0)
    FROM position pos
    LEFT JOIN risk_metric rm ON pos.position_id = rm.position_id
    WHERE pos.portfolio_id = @portfolio_id;
    
    -- Calculate Herfindahl index (concentration measure)
    -- Formula: Sum of squared weights (as percentages)
    -- Range: 0.01 (100 equal positions) to 10000 (single position)
    SET @concentration_herfindahl = ROUND(@sum_weights_squared * 10000, 4);
    
    BEGIN TRY
        -- Check if snapshot for this date already exists
        IF EXISTS (SELECT 1 FROM exposure_snapshot 
                   WHERE portfolio_id = @portfolio_id AND snapshot_date = @snapshot_date)
        BEGIN
            -- Update existing snapshot
            UPDATE exposure_snapshot
            SET total_exposure = @total_exposure,
                var_1day_95 = @var_1day_95,
                var_1day_99 = @var_1day_99,
                var_10day_99 = @var_10day_99,
                largest_position_pct = @largest_position_pct,
                num_positions = @num_positions,
                concentration_herfindahl = @concentration_herfindahl
            WHERE portfolio_id = @portfolio_id AND snapshot_date = @snapshot_date;
        END
        ELSE
        BEGIN
            -- Insert new snapshot
            INSERT INTO exposure_snapshot 
                (portfolio_id, snapshot_date, total_exposure, var_1day_95, var_1day_99, 
                 var_10day_99, largest_position_pct, currency, num_positions, concentration_herfindahl)
            VALUES 
                (@portfolio_id, @snapshot_date, @total_exposure, @var_1day_95, @var_1day_99,
                 @var_10day_99, @largest_position_pct, @base_currency, @num_positions, @concentration_herfindahl);
        END
        
        RETURN 0;
    END TRY
    BEGIN CATCH
        RAISERROR('Error storing snapshot', 16, 1);
        RETURN 1;
    END CATCH
END;

-- ============================================================================
-- STORED PROCEDURE 4: update_risk_metrics
-- Purpose: Update risk metrics for all positions in a portfolio
-- ============================================================================
CREATE PROCEDURE update_risk_metrics
    @portfolio_id INT,
    @metric_date DATE = NULL,
    @var_1d_95_multiplier DECIMAL(5,2) = 1.0
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @metric_check INT;
    
    -- Set metric date to today if not provided
    IF @metric_date IS NULL
        SET @metric_date = CAST(GETDATE() AS DATE);
    
    BEGIN TRY
        -- Update or insert risk metrics for positions
        UPDATE rm
        SET rm.var_1d_95 = ROUND(rm.var_1d_95 * @var_1d_95_multiplier, 2),
            rm.var_10d_99 = ROUND(rm.var_10d_99 * @var_1d_95_multiplier, 2)
        FROM risk_metric rm
        JOIN position pos ON rm.position_id = pos.position_id
        WHERE pos.portfolio_id = @portfolio_id
            AND rm.metric_date = @metric_date;
        
        RETURN 0;
    END TRY
    BEGIN CATCH
        RAISERROR('Error updating risk metrics', 16, 1);
        RETURN 1;
    END CATCH
END;

-- ============================================================================
-- STORED PROCEDURE 5: get_portfolio_summary
-- Purpose: Get comprehensive portfolio summary with exposures and risks
-- ============================================================================
CREATE PROCEDURE get_portfolio_summary
    @portfolio_id INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
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
        AND es.snapshot_date = (SELECT MAX(snapshot_date) FROM exposure_snapshot WHERE portfolio_id = p.portfolio_id)
    LEFT JOIN risk_limit rl ON p.portfolio_id = rl.portfolio_id
    LEFT JOIN limit_breach lb ON rl.limit_id = lb.limit_id AND lb.status = 'OPEN'
    WHERE @portfolio_id IS NULL OR p.portfolio_id = @portfolio_id
    GROUP BY 
        p.portfolio_id, p.portfolio_code, p.portfolio_name, p.portfolio_type,
        p.base_currency, p.aum, p.manager, p.is_active,
        es.var_1day_95, es.var_1day_99, es.concentration_herfindahl, es.snapshot_date
    ORDER BY p.portfolio_id;
END;
