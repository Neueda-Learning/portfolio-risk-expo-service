import type { LimitBreach } from "@/types";
import { apiFetch } from "./fetch";
import {CONSTANTS} from "@/lib/constants";

export async function getOpenBreaches(): Promise<LimitBreach[]> {
  return apiFetch<LimitBreach[]>(`${CONSTANTS.routes.breaches}?status=OPEN`);
}

export async function getBreaches(params?: {
  status?: string;
  portfolioId?: number;
}): Promise<LimitBreach[]> {
  const qs = new URLSearchParams();
  if (params?.status) qs.set("status", params.status);
  if (params?.portfolioId) qs.set("portfolioId", String(params.portfolioId));
  return apiFetch<LimitBreach[]>(`${CONSTANTS.routes.breaches}?${qs}`);
}
