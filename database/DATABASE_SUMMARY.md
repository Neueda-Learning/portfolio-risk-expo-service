# DATABASE SUMMARY

## Structure

**15 Tables:** <br>

  Core Tables (8)
  
    - portfolio
    - position
    - risk_factor
    - risk_metric
    - stress_scenario
    - stress_result
    - risk_limit
    - limit_breach

  Master/Lookup Tables (7)
  
    - asset_class
    - instrument
    - currency
    - exchange_rate
    - price_history
    - exposure_snapshot
    - scenario_risk_factor

  Junction Table (1)
  
    - scenario_risk_factor (many-to-many: stress_scenario <-> risk_factor)
      - 75 records mapping 5 scenarios to 20 risk factors
      - Enables flexible scenario composition
---

**20 Foreign Keys:**

  - portfolio → currency(base_currency)
  - position → portfolio(portfolio_id)
  - position → instrument(instrument_id)
  - instrument → asset_class(asset_class_id)
  - instrument → currency(currency)
  - risk_factor → asset_class(asset_class_id)
  - risk_factor → currency(currency)
  - exchange_rate → currency(from_currency)
  - exchange_rate → currency(to_currency)
  - price_history → instrument(instrument_id)
  - exposure_snapshot → portfolio(portfolio_id)
  - risk_limit → portfolio(portfolio_id)
  - risk_metric → position(position_id)
  - stress_result → portfolio(portfolio_id)
  - stress_result → stress_scenario(scenario_id)
  - stress_result → instrument(worst_position_instrument_id)
  - limit_breach → risk_limit(limit_id)
  - limit_breach → portfolio(portfolio_id)
  - scenario_risk_factor → stress_scenario(scenario_id)
  - scenario_risk_factor → risk_factor(factor_id)

---

**6 Database Views:**
  - limit_breaches_vw - Open breaches with severity ranking
  - exposure_by_asset_class_vw - Aggregated exposure per asset class
  - portfolio_risk_summary_vw - Portfolio-level risk metrics
  - scenario_impact_analysis_vw - Scenario shocks on risk factors
  - position_detail_vw - Full position details with latest prices
  - stress_test_results_vw - Stress test impacts and worst positions

---

**5 Stored Procedures**:
  - calculate_exposure - Sum positions with FX conversion to base currency
  - check_limits - Compare exposures against limits, insert breaches
  - store_snapshot - Create daily exposure snapshot with VaR metrics
  - update_risk_metrics - Recalculate position-level risk metrics
  - get_portfolio_summary - Retrieve comprehensive portfolio summary

## Data

**380+ Records:**
  - 20 portfolios (multiple currencies, types, AUM values)
  - 5 currencies (USD, GBP, EUR, HKD, JPY)
  - 5 asset classes (Equities, Fixed Income, FX, Commodities, Alternative)
  - 18 instruments (stocks, bonds, FX pairs, commodities)
  - 20 positions (normalized with instrument_id FK)
  - 20 risk factors (equities, rates, FX, volatility, commodities)
  - 5 stress scenarios (historical, hypothetical, regulatory)
  - 75 scenario-factor mappings (many-to-many with shocks)
  - 20 risk metrics (var_1d_95, pv01, delta, vega, gamma)
  - 10 stress results (portfolio-level P&L impacts)
  - 10 risk limits (VAR, concentration, duration, leverage limits)
  - 4 limit breaches (tracked with status and resolution)
  - 33 price history records (daily close prices by instrument)
  - 60 exposure snapshots (end-of-day totals with VaR and concentration)
  - 18 exchange rates (currency pairs as of 2026-03-20)

## Load sequence

**18 Files (in strict dependency order):**
```
  - 00_ddl_schema.sql - All table definitions with proper FK ordering
  - 01_data_currencies.sql - Currency master (5 records)
  - 02_data_portfolios.sql - Portfolios (20 records, bonds to currency)
  - 03_data_exchange_rates.sql - FX rates (18 records, bonds to currency)
  - 04_data_asset_classes.sql - Asset classes (5 records)
  - 05_data_instruments.sql - Instruments (18 records, bonds to asset_class, currency)
  - 06_data_positions.sql - Positions (20 records, normalized via instrument_id)
  - 07_data_risk_factors.sql - Risk factors (20 records, bonds to asset_class, currency)
  - 08_data_stress_scenarios.sql - Scenarios (5 records)
  - 09_data_scenario_risk_factors.sql - Junction table (75 records)
  - 10_data_risk_metrics.sql - Risk metrics (20 records, bonds to position)
  - 11_data_stress_results.sql - Stress results (10 records, bonds to portfolio, scenario, instrument)
  - 12_data_risk_limits.sql - Limits (10 records, bonds to portfolio)
  - 13_data_limit_breaches.sql - Breaches (7 records, bonds to limit, portfolio)
  - 14_data_price_history.sql - Price history (33 records, bonds to instrument)
  - 15_data_exposure_snapshots.sql - Snapshots (60 records, bonds to portfolio)
  - 16_create_views.sql - 6 views for analysis
  - 17_create_stored_procedures.sql - 5 procedures for automation
```

## Key Architectural Achievements

**Full Normalization (3NF compliance):**
   - Eliminated denormalized columns from position table
   - Proper master/detail relationships
   - Single source of truth for all lookup data

**Multi-Currency Support:**
   - Currency master table (ISO 4217 codes)
   - Exchange rate historical tracking (by date)
   - FX conversion logic in calculate_exposure procedure
   - All portfolio/instrument/risk_factor currencies bonded to master

**Referential Integrity (100%):**
   - 20 foreign key constraints enforced
   - All data validated against constraints
   - No orphaned records in database
   - Cascade behaviors defined appropriately

**Many-to-Many Relationships:**
   - Junction table for stress scenarios ↔ risk factors
   - Supports flexible scenario composition
   - Eliminates denormalization

**Type Safety:**
   - All currency codes validated via FK
   - Asset classes enumerated in master table
   - CHECK constraints on status/type fields
   - UNIQUE constraints on natural keys

**Audit Trail:**
   - created_at timestamps on key tables
   - updated_at timestamps for change tracking
   - Historical rate tracking in exchange_rate table
   - Enables reproducible calculations

## Benefits for Risk Management

- Single portfolio view across all currencies
- Automated exposure calculations with FX conversion
- Flexible stress scenario composition
- Historical price and rate tracking
- Audit trail of all calculations
- Daily automated risk snapshots
- Limit breach detection and tracking
- Extensible schema (easy to add new asset classes, currencies)
