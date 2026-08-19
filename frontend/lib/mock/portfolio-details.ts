import type { Portfolio, PortfolioType } from "@/types";

export interface PortfolioPosition {
  positionId: number;
  instrumentId: number;
  instrumentName: string;
  instrumentIsin: string;
  assetClass: string;
  sector: string | null;
  issuer: string;
  currency: string;
  quantity: number;
  marketPrice: number;
  marketValue: number;
  marketValueBase: number;
  weightPct: number;
  costBasis: number;
  positionDate: string;
}

export interface PortfolioDetails extends Portfolio {
  asOfDate: string;
  positions: PortfolioPosition[];
}

const portfolioCatalog: Record<
  number,
  Pick<Portfolio, "portfolioId" | "portfolioCode" | "portfolioName" | "portfolioType" | "baseCurrency" | "aum" | "benchmark" | "riskMandate" | "manager" | "isActive">
> = {
  1: {
    portfolioId: 1,
    portfolioCode: "EQ-UK",
    portfolioName: "UK Equity Fund",
    portfolioType: "EQUITY",
    baseCurrency: "GBP",
    aum: 1500000000,
    benchmark: "FTSE 100",
    riskMandate: "TR: ±200bps, Max Sector: 15%",
    manager: "John Smith",
    isActive: true,
  },
  2: {
    portfolioId: 2,
    portfolioCode: "EQ-US",
    portfolioName: "US Equity Fund",
    portfolioType: "EQUITY",
    baseCurrency: "USD",
    aum: 2000000000,
    benchmark: "S&P 500",
    riskMandate: "TR: ±250bps, Max Sector: 15%",
    manager: "Sarah Johnson",
    isActive: true,
  },
  3: {
    portfolioId: 3,
    portfolioCode: "FI-GC",
    portfolioName: "Global Credit Fund",
    portfolioType: "FIXED_INCOME",
    baseCurrency: "GBP",
    aum: 3500000000,
    benchmark: "Bloomberg Aggregate",
    riskMandate: "Duration: 4-6y, Spread: ±100bps",
    manager: "Michael Chen",
    isActive: true,
  },
  4: {
    portfolioId: 4,
    portfolioCode: "MA-BAL",
    portfolioName: "Balanced Multi-Asset",
    portfolioType: "MULTI_ASSET",
    baseCurrency: "GBP",
    aum: 2500000000,
    benchmark: "60/40 Index",
    riskMandate: "VAR 1D 95%: £5-8M, Max Leverage: 2x",
    manager: "Emma Wilson",
    isActive: true,
  },
  5: {
    portfolioId: 5,
    portfolioCode: "HF-ALT",
    portfolioName: "Alternative Strategies",
    portfolioType: "HEDGE",
    baseCurrency: "USD",
    aum: 800000000,
    benchmark: "HFRI Fund of Funds Index",
    riskMandate: "Max DD: 10%, VAR 1D 95%: 1% AUM",
    manager: "Robert Taylor",
    isActive: true,
  },
  6: {
    portfolioId: 6,
    portfolioCode: "MM-EUR",
    portfolioName: "Euro Money Market",
    portfolioType: "MONEY_MKT",
    baseCurrency: "EUR",
    aum: 1200000000,
    benchmark: "EONIA",
    riskMandate: "Weighted Avg Maturity: 30-90d",
    manager: "Lisa Anderson",
    isActive: true,
  },
  7: {
    portfolioId: 7,
    portfolioCode: "EQ-AP",
    portfolioName: "Asia-Pacific Equity",
    portfolioType: "EQUITY",
    baseCurrency: "USD",
    aum: 1800000000,
    benchmark: "MSCI AP ex-JP",
    riskMandate: "TR: ±300bps, Max Sector: 12%",
    manager: "David Park",
    isActive: true,
  },
  8: {
    portfolioId: 8,
    portfolioCode: "FI-GOVT",
    portfolioName: "Government Bonds",
    portfolioType: "FIXED_INCOME",
    baseCurrency: "GBP",
    aum: 2200000000,
    benchmark: "iBoxx Sovereigns",
    riskMandate: "Duration: 5-8y, Gilt only",
    manager: "Patricia Brown",
    isActive: true,
  },
  9: {
    portfolioId: 9,
    portfolioCode: "MA-FLEX",
    portfolioName: "Flexible Income",
    portfolioType: "MULTI_ASSET",
    baseCurrency: "GBP",
    aum: 1100000000,
    benchmark: "Customized Blend",
    riskMandate: "VAR 1D 95%: £2-3M, Leverage: ≤1.5x",
    manager: "Christopher Lee",
    isActive: true,
  },
  10: {
    portfolioId: 10,
    portfolioCode: "HF-MACRO",
    portfolioName: "Macro Hedge Fund",
    portfolioType: "HEDGE",
    baseCurrency: "GBP",
    aum: 650000000,
    benchmark: "CISDM Equal-Weight Index",
    riskMandate: "Max DD: 12%, Volatility: ≤12%",
    manager: "Victoria Martinez",
    isActive: true,
  },
  11: {
    portfolioId: 11,
    portfolioCode: "EQ-EMRG",
    portfolioName: "Emerging Markets Equity",
    portfolioType: "EQUITY",
    baseCurrency: "USD",
    aum: 950000000,
    benchmark: "MSCI EM",
    riskMandate: "TR: ±400bps, Max Sector: 10%",
    manager: "Andrew Zhang",
    isActive: true,
  },
  12: {
    portfolioId: 12,
    portfolioCode: "FI-CORP",
    portfolioName: "Corporate Bonds",
    portfolioType: "FIXED_INCOME",
    baseCurrency: "GBP",
    aum: 2800000000,
    benchmark: "iBoxx Corporate",
    riskMandate: "Duration: 3-5y, Min Rating: BBB-",
    manager: "Michelle Harris",
    isActive: true,
  },
  13: {
    portfolioId: 13,
    portfolioCode: "MA-GROWTH",
    portfolioName: "Growth Portfolio",
    portfolioType: "MULTI_ASSET",
    baseCurrency: "GBP",
    aum: 1900000000,
    benchmark: "70/30 Growth Index",
    riskMandate: "VAR 1D 95%: £7-10M, Max Leverage: 2.5x",
    manager: "Kevin Thompson",
    isActive: true,
  },
  14: {
    portfolioId: 14,
    portfolioCode: "HF-CTA",
    portfolioName: "Commodity Trading Fund",
    portfolioType: "HEDGE",
    baseCurrency: "USD",
    aum: 520000000,
    benchmark: "Barclay CTA Index",
    riskMandate: "Max DD: 15%, Corr to Equities: <0.3",
    manager: "Jennifer White",
    isActive: true,
  },
  15: {
    portfolioId: 15,
    portfolioCode: "EQ-EUR",
    portfolioName: "European Equity",
    portfolioType: "EQUITY",
    baseCurrency: "EUR",
    aum: 1650000000,
    benchmark: "STOXX 600",
    riskMandate: "TR: ±250bps, Max Sector: 14%",
    manager: "Marco Rossi",
    isActive: true,
  },
  16: {
    portfolioId: 16,
    portfolioCode: "FI-HY",
    portfolioName: "High Yield Bonds",
    portfolioType: "FIXED_INCOME",
    baseCurrency: "GBP",
    aum: 1400000000,
    benchmark: "iBoxx High Yield",
    riskMandate: "Duration: 3-4y, Min Rating: B-",
    manager: "Diana Garcia",
    isActive: true,
  },
  17: {
    portfolioId: 17,
    portfolioCode: "MA-CONS",
    portfolioName: "Conservative Income",
    portfolioType: "MULTI_ASSET",
    baseCurrency: "GBP",
    aum: 1300000000,
    benchmark: "50/50 Conservative Index",
    riskMandate: "VAR 1D 95%: £1.5-2.5M, Leverage: ≤1.2x",
    manager: "Steven O'Brien",
    isActive: true,
  },
  18: {
    portfolioId: 18,
    portfolioCode: "HF-EVENTS",
    portfolioName: "Event-Driven Fund",
    portfolioType: "HEDGE",
    baseCurrency: "GBP",
    aum: 680000000,
    benchmark: "HFRI Event-Driven Index",
    riskMandate: "Max DD: 8%, Target Return: 8%+",
    manager: "Rachel King",
    isActive: true,
  },
  19: {
    portfolioId: 19,
    portfolioCode: "EQ-JAPAN",
    portfolioName: "Japan Equity Fund",
    portfolioType: "EQUITY",
    baseCurrency: "JPY",
    aum: 850000000,
    benchmark: "Nikkei 225",
    riskMandate: "TR: ±280bps, Max Sector: 13%",
    manager: "Hiroshi Tanaka",
    isActive: true,
  },
  20: {
    portfolioId: 20,
    portfolioCode: "FI-MIXED",
    portfolioName: "Mixed Fixed Income",
    portfolioType: "FIXED_INCOME",
    baseCurrency: "GBP",
    aum: 2600000000,
    benchmark: "Bloomberg Multi-Asset",
    riskMandate: "Duration: 4-7y, Blended Rating: A-",
    manager: "Susan Blake",
    isActive: true,
  },
};

const instrumentCatalog = [
  {
    instrumentId: 1,
    instrumentName: "Unilever PLC",
    instrumentIsin: "GB00B10RZP78",
    assetClass: "Equity",
    sector: "Consumer Staples",
    issuer: "Unilever",
    currency: "GBP",
    referencePrice: 485.5,
  },
  {
    instrumentId: 2,
    instrumentName: "BP plc",
    instrumentIsin: "GB0008325020",
    assetClass: "Equity",
    sector: "Energy",
    issuer: "BP",
    currency: "GBP",
    referencePrice: 510.2,
  },
  {
    instrumentId: 3,
    instrumentName: "Apple Inc.",
    instrumentIsin: "US0378691033",
    assetClass: "Equity",
    sector: "Information Technology",
    issuer: "Apple",
    currency: "USD",
    referencePrice: 195.8,
  },
  {
    instrumentId: 4,
    instrumentName: "Microsoft Corp",
    instrumentIsin: "US5949181045",
    assetClass: "Equity",
    sector: "Information Technology",
    issuer: "Microsoft",
    currency: "USD",
    referencePrice: 422.6,
  },
  {
    instrumentId: 5,
    instrumentName: "BNP Paribas Senior Notes",
    instrumentIsin: "XS2450000000",
    assetClass: "Bond",
    sector: "Financials",
    issuer: "BNP Paribas",
    currency: "EUR",
    referencePrice: 98.75,
  },
  {
    instrumentId: 6,
    instrumentName: "Apple Inc Notes 2.25%",
    instrumentIsin: "US037833AJ60",
    assetClass: "Bond",
    sector: "Information Technology",
    issuer: "Apple",
    currency: "USD",
    referencePrice: 101.2,
  },
  {
    instrumentId: 7,
    instrumentName: "Siemens AG",
    instrumentIsin: "DE0007236101",
    assetClass: "Equity",
    sector: "Industrials",
    issuer: "Siemens",
    currency: "EUR",
    referencePrice: 165.3,
  },
  {
    instrumentId: 8,
    instrumentName: "EUR/USD FX Spot",
    instrumentIsin: "EURUSD000000",
    assetClass: "FX",
    sector: null,
    issuer: "ECB/Federal Reserve",
    currency: "USD",
    referencePrice: 1.085,
  },
  {
    instrumentId: 9,
    instrumentName: "VSTOXX Volatility Future",
    instrumentIsin: "XX0000000001",
    assetClass: "Derivative",
    sector: null,
    issuer: "Eurex",
    currency: "EUR",
    referencePrice: 18.5,
  },
  {
    instrumentId: 10,
    instrumentName: "German T-Bills 3M",
    instrumentIsin: "DE0001121574",
    assetClass: "Bond",
    sector: "Sovereign",
    issuer: "German Government",
    currency: "EUR",
    referencePrice: 99.92,
  },
  {
    instrumentId: 11,
    instrumentName: "French T-Bills 6M",
    instrumentIsin: "FR0000470143",
    assetClass: "Bond",
    sector: "Sovereign",
    issuer: "French Government",
    currency: "EUR",
    referencePrice: 99.85,
  },
  {
    instrumentId: 12,
    instrumentName: "AIA Group Ltd",
    instrumentIsin: "HK0000823308",
    assetClass: "Equity",
    sector: "Financials",
    issuer: "AIA",
    currency: "HKD",
    referencePrice: 55.3,
  },
  {
    instrumentId: 13,
    instrumentName: "Toyota Motor Corp",
    instrumentIsin: "JP3436100006",
    assetClass: "Equity",
    sector: "Industrials",
    issuer: "Toyota",
    currency: "JPY",
    referencePrice: 2450,
  },
  {
    instrumentId: 14,
    instrumentName: "UK Gilts 4.75% 2037",
    instrumentIsin: "GB0030045733",
    assetClass: "Bond",
    sector: "Sovereign",
    issuer: "UK Government",
    currency: "GBP",
    referencePrice: 103.5,
  },
  {
    instrumentId: 15,
    instrumentName: "German Bunds 2.0% 2041",
    instrumentIsin: "DE0001102382",
    assetClass: "Bond",
    sector: "Sovereign",
    issuer: "German Government",
    currency: "EUR",
    referencePrice: 97.25,
  },
  {
    instrumentId: 16,
    instrumentName: "FTSE All-Share ETF",
    instrumentIsin: "GB0031862092",
    assetClass: "Equity",
    sector: null,
    issuer: "iShares",
    currency: "GBP",
    referencePrice: 4200,
  },
  {
    instrumentId: 17,
    instrumentName: "GBP/USD FX Spot",
    instrumentIsin: "GBPUSD000000",
    assetClass: "FX",
    sector: null,
    issuer: "Bank of England/Federal Reserve",
    currency: "USD",
    referencePrice: 1.265,
  },
  {
    instrumentId: 18,
    instrumentName: "Gold Spot Commodity",
    instrumentIsin: "GOLDSPOT0000",
    assetClass: "Commodity",
    sector: null,
    issuer: "LBMA",
    currency: "USD",
    referencePrice: 2050.5,
  },
];

const positionPattern = [28, 22, 16, 14, 12, 8];

function round(value: number, digits: number): number {
  const factor = 10 ** digits;
  return Math.round(value * factor) / factor;
}

function buildPositions(portfolioId: number, aum: number): PortfolioPosition[] {
  const offset = (portfolioId - 1) % instrumentCatalog.length;

  return positionPattern.map((weightPct, index) => {
    const instrument = instrumentCatalog[(offset + index) % instrumentCatalog.length];
    const weightedValue = round((aum * weightPct) / 100, 2);
    const marketPrice = round(instrument.referencePrice * (1 + ((portfolioId + index) % 5) * 0.01), 4);
    const quantity = round(weightedValue / marketPrice, 2);
    const costBasis = round(weightedValue * (0.96 + index * 0.005), 2);

    return {
      positionId: portfolioId * 100 + index + 1,
      instrumentId: instrument.instrumentId,
      instrumentName: instrument.instrumentName,
      instrumentIsin: instrument.instrumentIsin,
      assetClass: instrument.assetClass,
      sector: instrument.sector,
      issuer: instrument.issuer,
      currency: instrument.currency,
      quantity,
      marketPrice,
      marketValue: weightedValue,
      marketValueBase: weightedValue,
      weightPct,
      costBasis,
      positionDate: "2026-03-20",
    };
  });
}

function buildFallbackPortfolio(portfolioId: number): Portfolio {
  return {
    portfolioId,
    portfolioCode: `PORT-${portfolioId}`,
    portfolioName: `Portfolio ${portfolioId}`,
    portfolioType: "EQUITY" as PortfolioType,
    baseCurrency: "GBP",
    aum: 1000000000,
    benchmark: "Custom Benchmark",
    riskMandate: "Mock data",
    manager: "Portfolio Manager",
    isActive: true,
  };
}

export function getMockPortfolioDetails(portfolioId: number): PortfolioDetails {
  const portfolio = portfolioCatalog[portfolioId] ?? buildFallbackPortfolio(portfolioId);

  return {
    ...portfolio,
    asOfDate: "2026-03-20",
    positions: buildPositions(portfolioId, portfolio.aum),
  };
}
