import type {
  Portfolio,
  PortfolioAssetExposure,
  PortfolioExposure,
  PortfolioLimitsResponse,
  PortfolioPositionRow,
  PortfolioSectorExposure,
  PortfolioStatsData,
  PortfolioVar,
} from "@/types";
import { apiFetch } from "./client";
import {CONSTANTS} from "@/lib/constants";

export async function getPortfolios(): Promise<Portfolio[]> {
  return apiFetch<Portfolio[]>(CONSTANTS.routes.portfolios);
}

export async function getPortfolioId(
    portfolioId: number
): Promise<Portfolio> {
  return apiFetch<Portfolio>(`${CONSTANTS.routes.portfolios}/${portfolioId}`);
}

export async function getPositionsFromPortfolioId(
    portfolioId: number,
): Promise<PortfolioPositionRow[]> {
  return apiFetch<PortfolioPositionRow[]>(`${CONSTANTS.routes.portfolios}/${portfolioId}/positions`);
}

export async function getPortfolioExposure(
  portfolioId: number
): Promise<PortfolioExposure> {
  return apiFetch<PortfolioExposure>(`${CONSTANTS.routes.portfolios}/${portfolioId}/exposure`);
}

export async function getPortfolioLimits(
  portfolioId: number
): Promise<PortfolioLimitsResponse> {
  return apiFetch<PortfolioLimitsResponse>(`${CONSTANTS.routes.portfolios}/${portfolioId}/limits`);
}

export async function getPortfolioExposureBySector(
  portfolioId: number
): Promise<PortfolioSectorExposure> {
  return apiFetch<PortfolioSectorExposure>(`${CONSTANTS.routes.portfolios}/${portfolioId}/exposure/by-sector`);
}

export async function getPortfolioExposureByAsset(
  portfolioId: number
): Promise<PortfolioAssetExposure> {
  return apiFetch<PortfolioAssetExposure>(`${CONSTANTS.routes.portfolios}/${portfolioId}/exposure/by-asset`);
}

export async function getPortfolioVar(
  portfolioId: number,
  confidence: 95 | 99
): Promise<PortfolioVar> {
  return apiFetch<PortfolioVar>(
    `${CONSTANTS.routes.portfolios}/${portfolioId}/var?confidence=${confidence}`
  );
}

export async function getPortfolioStats(
  portfolioId: number,
  confidence: 95 | 99 = 95
): Promise<PortfolioStatsData> {
  const [exposureResult, limitsResult, varResult, sectorResult, assetResult] =
    await Promise.allSettled([
      getPortfolioExposure(portfolioId),
      getPortfolioLimits(portfolioId),
      getPortfolioVar(portfolioId, confidence),
      getPortfolioExposureBySector(portfolioId),
      getPortfolioExposureByAsset(portfolioId),
    ]);

  if (exposureResult.status !== "fulfilled") {
    throw exposureResult.reason;
  }
  if (limitsResult.status !== "fulfilled") {
    throw limitsResult.reason;
  }
  if (sectorResult.status !== "fulfilled") {
    throw sectorResult.reason;
  }
  if (assetResult.status !== "fulfilled") {
    throw assetResult.reason;
  }

  return {
    exposure: exposureResult.value,
    limits: limitsResult.value,
    var: varResult.status === "fulfilled" ? varResult.value : null,
    sectorExposure: sectorResult.value,
    assetExposure: assetResult.value,
  };
}
