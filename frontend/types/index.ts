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

// ─── Instrument ───────────────────────────────────────────────────────────────

export interface Instrument {
  instrumentId: number;
  instrumentIsin?: string;
  instrumentName: string;
  currency?: string;
  issueDate?: string;
  maturityDate?: string;
  issuer?: string;
  sector?: string | null;
  assetClass?: string;
  assetClassId?: number;
  isActive?: boolean;
  createdAt?: string;
}

// ─── Portfolio Position ────────────────────────────────────────────────────────

export interface Position {
  positionId: number;
  portfolioId: number;
  instrumentId: number;
  positionDate: string;
  quantity: number;
  marketPrice: number;
  marketValue: number;
  marketValueBase: number;
  weightPct: number;
  costBasis?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PortfolioPositionApiRow extends Position {
  instrumentName: string;
}

export interface PortfolioPositionWithInstrument extends Position {
  instrument: Instrument;
}

// ─── Portfolio Details ────────────────────────────────────────────────────────

export interface PortfolioDetails extends Portfolio {
  positions: PortfolioPositionWithInstrument[];
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
