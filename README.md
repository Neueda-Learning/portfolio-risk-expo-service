# Project 09 – Portfolio Risk & Exposure Service

> **Starter / reference data — not the solution schema.**
>
> This pack is realistic domain sample data so you can explore the problem space and seed demos.
> Your Sprint 1 deliverable is still to design **your own** E/R model and DDL (see the project brief).
> You may load this pack into a scratch database, then map or transform useful rows into *your* schema —
> or invent your own seed data inspired by it. Do not treat this DDL as the answer to §2.2.

## SQL Data Files

### Load Order
Execute files in this order:
1. `database/data/00_ddl_schema.sql` — Table definitions
2. `database/data/01_data_portfolios.sql` — 20 portfolios (equity, fixed income, multi-asset, hedge, money market)
3. `database/data/02_data_positions.sql` — 20 current holdings
4. `database/data/03_data_risk_factors.sql` — 20 market risk factors (indices, rates, spreads, FX, volatility, commodities)
5. `database/data/04_data_risk_metrics.sql` — VaR, Greeks, expected shortfall per position
6. `database/data/05_data_stress_scenarios.sql` — 5 predefined stress scenarios (GFC 2008, COVID, Brexit, Ukraine, Rate Shock)
7. `database/data/06_data_stress_results.sql` — P&L impact and VaR changes per portfolio/scenario
8. `database/data/07_data_risk_limits.sql` — 10 risk limits (VAR, concentration, duration, leverage, drawdown, sector)
9. `database/data/08_data_limit_breaches.sql` — 4 limit breach events (1 critical VAR breach, 1 critical leverage breach, 2 major)

### Table Descriptions

#### portfolio
**Sample investment portfolios for teaching.**
- `portfolio_type`: EQUITY, FIXED_INCOME, MULTI_ASSET, HEDGE, MONEY_MKT
- `aum`: Assets under management in base currency
- `base_currency`: Reporting currency (GBP, USD, EUR, JPY)
- **Rows**: 20 portfolios spanning £500M–£5Bn AUM

#### position
**Current holdings within each portfolio.**
- `asset_class`: EQUITY, FIXED_INCOME, FX, DERIVATIVE, COMMODITY
- `market_value_base`: Converted to portfolio base currency using FX rates
- `weight_pct`: Percentage of portfolio AUM
- `cost_basis`: Acquisition cost for P&L tracking
- **Rows**: 20 positions across FTSE 100, S&P 500, Gilts, Bunds, corporate bonds, FX, derivatives

#### risk_factor
**Market risk factors driving portfolio returns and risks.**
- `factor_type`: EQUITY_INDEX, INTEREST_RATE, CREDIT_SPREAD, FX, VOLATILITY, COMMODITY
- `change_pct`: Daily movement as % or basis points equivalent
- **Rows**: 20 factors including FTSE 100, S&P 500, EUR/USD, GBP/USD, 10Y gilts/bunds, iTraxx, VIX, commodities

#### risk_metric
**Risk analytics per position (Greeks, Value-at-Risk, sensitivities).**
- `var_1d_95`: 1-day 95% Value-at-Risk (conservative daily loss estimate)
- `var_10d_99`: 10-day 99% VaR (regulatory capital requirement horizon)
- `pv01`: Price value of 1 basis point (fixed income sensitivity)
- `cs01`: Credit spread 01 (credit instrument sensitivity)
- `delta`, `gamma`, `vega`: Greek letters for derivatives
- `expected_shortfall`: Average loss exceeding VaR (tail risk)
- `methodology`: PARAMETRIC (covariance), HISTORICAL (actual losses), MONTE_CARLO (simulation)
- **Rows**: 20 (one per position)

#### stress_scenario
**Predefined extreme market scenarios for portfolio stress testing.**
- **5 scenarios**:
  - GFC 2008: Equities −50%, spreads +300bps, rates −100bps
  - COVID-19 2020: Equities −35%, spreads +200bps, rates −50bps
  - Brexit: GBP −10%, equities −12%, spreads +100bps
  - Russia-Ukraine: Oil +50%, gas +100%, equities −15%
  - Hypothetical Rate Shock: Yields +200bps, bonds −15%, spreads +50bps

#### stress_result
**Portfolio P&L and risk metric changes under each stress scenario.**
- `pnl_impact`: Estimated P&L change in base currency
- `var_pre` / `var_post`: VaR before and after shock
- `worst_position_isin`: Instrument hit hardest by scenario
- **Rows**: 10 (2 scenarios per portfolio sample)

#### risk_limit
**Risk limits and current utilization per portfolio.**
- `limit_type`: VAR, CONCENTRATION, DURATION, LEVERAGE, DRAWDOWN, SECTOR_EXPOSURE
- `utilisation_pct`: Current value / limit value × 100%
- `status`: OK, WARNING, BREACH, SUSPENDED
  - **OK**: < warning threshold
  - **WARNING**: warning threshold to limit
  - **BREACH**: exceeded limit (requires action)
  - **SUSPENDED**: limit temporarily frozen
- **Rows**: 10 limits

#### limit_breach
**Triggered limit breach events (4 active breaches).**
- `severity`: MINOR (< 5% excess), MAJOR (5–25% excess), CRITICAL (> 25% excess)
- `status`: OPEN (unack), ACKNOWLEDGED (in progress), RESOLVED (remedied), WAIVED (approved exception)
- **Rows**: 4 breaches
  - Breach 1 (CRITICAL): VAR exceeded by 750k in Portfolio 3
  - Breach 2 (MAJOR): Leverage 102.5% in Portfolio 5
  - Breach 3 (CRITICAL): VAR exceeded by 100k in Portfolio 10 (OPEN)
  - Breach 4 (MAJOR): Top 10 concentration at 108.75%

---

## Domain Concepts

### Value-at-Risk (VaR)
**VaR(X%, t-day)** = maximum loss within time horizon t at confidence level X%
- VaR(95%, 1-day): "With 95% confidence, daily loss ≤ VaR"
- VaR(99%, 10-day): Regulatory Pillar 3 horizon; captures larger moves
- Common in risk reports; basis for regulatory capital and limit monitoring

### Greeks (Derivatives)
- **Delta (Δ)**: Price sensitivity to 1-unit move in underlying (equity delta ≈ 0–1)
- **Gamma (Γ)**: Rate of change of delta (convexity); increases with volatility
- **Vega (ν)**: Sensitivity to 1% change in volatility; large for options
- Used to hedge derivatives; critical for tail risk management

### Credit Spread Sensitivity (CS01)
**CS01** = P&L change per 1 basis point move in credit spread
- Essential for corporate bond, CDS, subordinated debt portfolios
- Higher CS01 = greater credit risk exposure

### Expected Shortfall (ES)
**ES** = average loss conditional on VaR breach
- More sensitive to tail events than VaR (includes worst losses beyond VaR)
- Basel III regulatory emphasis for capital adequacy

### Stress Testing
**Deterministic extreme scenarios** (not probabilistic) applied to all positions:
- Historical (2008 GFC, 2020 COVID, Brexit): real past events
- Hypothetical (Rate Shock +200bps): plausible but not yet observed
- Reveals portfolio vulnerabilities and tail correlation breakdowns

### Limit Breach Workflow
1. **Limit Definition**: Max risk metric allowed (e.g., VAR ≤ £8M)
2. **Monitoring**: Daily check of current value vs. limit
3. **Warning Threshold**: Pre-limit level (e.g., 75% of limit) triggers alerts
4. **Breach**: Exceeds limit; requires management acknowledgment and action plan
5. **Resolution**: Position trimmed, hedged, or limit waived (with approval)

---

## Sample Queries

### Query 1: Portfolio Risk Summary
Show each portfolio's AUM, current VAR, and limit status:

\`\`\`sql
SELECT
    p.portfolio_code,
    p.portfolio_name,
    p.aum,
    SUM(CASE WHEN rm.var_1d_95 IS NOT NULL THEN rm.var_1d_95 ELSE 0 END) AS total_var_1d_95,
    COUNT(DISTINCT CASE WHEN rl.status = 'BREACH' THEN rl.limit_id END) AS breach_count,
    MAX(CASE WHEN rl.status = 'WARNING' THEN 'YES' ELSE 'NO' END) AS has_warnings
FROM portfolio p
LEFT JOIN position pos ON p.portfolio_id = pos.portfolio_id
LEFT JOIN risk_metric rm ON pos.position_id = rm.position_id
LEFT JOIN risk_limit rl ON p.portfolio_id = rl.portfolio_id
WHERE p.is_active = TRUE
GROUP BY p.portfolio_id, p.portfolio_code, p.portfolio_name, p.aum
ORDER BY total_var_1d_95 DESC;
\`\`\`

**Output**: Risk profile of each active fund; highlights high-risk and limit-breach portfolios.

---

### Query 2: Stress Test Results Ranking
Which portfolios and scenarios cause largest P&L losses?

\`\`\`sql
SELECT
    sr.result_id,
    p.portfolio_code,
    ss.scenario_name,
    sr.pnl_impact,
    sr.pnl_impact_pct,
    sr.worst_position_isin,
    sr.worst_position_impact
FROM stress_result sr
INNER JOIN portfolio p ON sr.portfolio_id = p.portfolio_id
INNER JOIN stress_scenario ss ON sr.scenario_id = ss.scenario_id
ORDER BY ABS(sr.pnl_impact) DESC
LIMIT 10;
\`\`\`

**Output**: Top 10 stress results by magnitude; identifies most vulnerable portfolio/scenario pairs.

---

### Query 3: Open Limit Breaches & Remediation Status
What breaches are still unresolved, and what's the remediation plan?

\`\`\`sql
SELECT
    lb.breach_id,
    p.portfolio_code,
    rl.limit_type,
    lb.breach_date,
    lb.actual_value,
    lb.limit_value,
    lb.excess_amount,
    lb.severity,
    lb.acknowledged_by,
    lb.acknowledged_at,
    lb.resolution,
    lb.status
FROM limit_breach lb
INNER JOIN risk_limit rl ON lb.limit_id = rl.limit_id
INNER JOIN portfolio p ON lb.portfolio_id = p.portfolio_id
WHERE lb.status IN ('OPEN', 'ACKNOWLEDGED')
ORDER BY lb.breach_date DESC, lb.severity DESC;
\`\`\`

**Output**: Active incidents; shows who acknowledged each and next action needed.

---

## Notes for Analysts

- **FX Conversion**: All positions converted to portfolio base_currency at 2026-03-20 spot rates
- **VaR Methodology Mix**: Parametric (fastest, assumes normality), Historical (robust, data-hungry), Monte Carlo (flexible, CPU-intensive)
- **Position Concentration**: Largest position = BP 13.61% of EQ-UK fund; within limit but material risk
- **Critical Breaches**: Portfolio 3 (FI-GC) VAR and Portfolio 10 (HF-MACRO) leverage require immediate escalation
- **Stress Scenarios**: All 5 scenarios active (is_active=TRUE); used in quarterly Risk Committee reviews
