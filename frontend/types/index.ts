// ─── Portfolio ────────────────────────────────────────────────────────────────

export type PortfolioType =
  | "EQUITY"
  | "FIXED_INCOME"
  | "MULTI_ASSET"
  | "HEDGE"
  | "MONEY_MKT";

export interface Portfolio {
  portfolioId: number;
  portfolioCode: string;
  portfolioName: string;
  portfolioType: PortfolioType;
  baseCurrency: string;
  aum: number;
  benchmark: string;
  riskMandate: string;
  manager: string;
  isActive: boolean;
}

// ─── Exposure ─────────────────────────────────────────────────────────────────

export interface PortfolioExposure {
  portfolioId: number;
  totalExposure: number;
  currency: string;
  positionCount: number;
}

// ─── VaR ──────────────────────────────────────────────────────────────────────

export interface PortfolioVar {
  portfolioId: number;
  confidence: number;
  var1Day: number;
  currency: string;
}

// ─── Limit Breach ─────────────────────────────────────────────────────────────

export type BreachSeverity = "CRITICAL" | "MAJOR" | "MINOR";
export type BreachStatus = "OPEN" | "ACKNOWLEDGED" | "RESOLVED";

export interface LimitBreach {
  breachId: number;
  limitId: number;
  portfolioId: number;
  portfolioName?: string;
  breachDate: string;
  limitValue: number;
  actualValue: number;
  excessAmount: number;
  severity: BreachSeverity;
  acknowledgedBy: string | null;
  acknowledgedAt: string | null;
  resolution: string | null;
  status: BreachStatus;
  limitType?: string;
}

// ─── Portfolio with aggregated summary (used for dashboard cards) ─────────────

export interface PortfolioSummary extends Portfolio {
  exposure?: PortfolioExposure;
  openBreachCount?: number;
}
