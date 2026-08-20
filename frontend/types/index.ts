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

export interface PortfolioPositionRow extends Position {
  instrumentName: string;
}

export interface PortfolioPositionWithInstrument extends Position {
  instrument: Instrument;
}

// ─── Portfolio Details ────────────────────────────────────────────────────────

export interface PortfolioOverview extends Portfolio {
  positions: PortfolioPositionWithInstrument[];
}

// ─── Exposure ─────────────────────────────────────────────────────────────────

export interface PortfolioExposure {
  portfolioId: number;
  portfolioName: string;
  totalExposure: number;
  currency: string;
  positionCount: number;
}

// ─── VaR ──────────────────────────────────────────────────────────────────────

export interface PortfolioVar {
  portfolioId: number;
  portfolioName: string;
  confidence: number;
  var1Day: number;
  currency: string;
}

export interface PortfolioLimitDetail {
  limitId: number;
  limitType: string;
  limitMetric: string;
  limitValue: number;
  warningThreshold: number;
  currentValue: number;
  utilisationPct: number;
  status: string;
  effectiveFrom: string;
  effectiveTo: string | null;
  isBreached: boolean;
}

export interface PortfolioLimitsResponse {
  portfolioId: number;
  portfolioName: string;
  totalExposure: number;
  baseCurrency: string;
  limits: PortfolioLimitDetail[];
}

export interface PortfolioSectorExposure {
  portfolioId: number;
  portfolioName: string;
  sectorExposures: Record<string, number>;
}

export interface PortfolioAssetExposure {
  portfolioId: number;
  portfolioName: string;
  assetExposures: Record<string, number>;
}

export interface PortfolioStatsData {
  exposure: PortfolioExposure;
  limits: PortfolioLimitsResponse;
  var: PortfolioVar | null;
  sectorExposure: PortfolioSectorExposure;
  assetExposure: PortfolioAssetExposure;
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
