import type {Portfolio, PortfolioExposure, Position, PortfolioVar} from "@/types";
import { apiFetch } from "./client";

export async function getPortfolios(): Promise<Portfolio[]> {
  return apiFetch<Portfolio[]>("/api/portfolios");
}

export async function getPortfolioId(
    portfolioId: number
): Promise<Portfolio> {
  return apiFetch<Portfolio>(`/api/portfolios/${portfolioId}`);
}

export async function getPositionsFromPortfolioId(
    portfolioId: number,
): Promise<Position[]> {
  return apiFetch<Position[]>(`/api/portfolios/${portfolioId}/positions`);
}

export async function getPortfolioExposure(
  portfolioId: number
): Promise<PortfolioExposure> {
  return apiFetch<PortfolioExposure>(`/api/portfolios/${portfolioId}/exposure`);
}

export async function getPortfolioVar(
  portfolioId: number,
  confidence: 95 | 99
): Promise<PortfolioVar> {
  return apiFetch<PortfolioVar>(
    `/api/portfolios/${portfolioId}/var?confidence=${confidence}`
  );
}
