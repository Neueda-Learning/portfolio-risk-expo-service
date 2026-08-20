import type {Portfolio, PortfolioExposure, PortfolioPositionApiRow, PortfolioVar} from "@/types";
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
): Promise<PortfolioPositionApiRow[]> {
  return apiFetch<PortfolioPositionApiRow[]>(`${CONSTANTS.routes.portfolios}/${portfolioId}/positions`);
}

export async function getPortfolioExposure(
  portfolioId: number
): Promise<PortfolioExposure> {
  return apiFetch<PortfolioExposure>(`${CONSTANTS.routes.portfolios}/${portfolioId}/exposure`);
}

export async function getPortfolioVar(
  portfolioId: number,
  confidence: 95 | 99
): Promise<PortfolioVar> {
  return apiFetch<PortfolioVar>(
    `${CONSTANTS.routes.portfolios}/${portfolioId}/var?confidence=${confidence}`
  );
}
