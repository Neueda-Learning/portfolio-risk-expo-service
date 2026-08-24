import { apiFetch } from "./fetch";
import { CONSTANTS } from "@/lib/constants";
import type { ExchangeRate } from "@/types";

export async function getTodayExchangeRates(): Promise<ExchangeRate[]> {
  return apiFetch<ExchangeRate[]>(`${CONSTANTS.routes.fxRatesApi}/today`);
}

export async function getExchangeRatesForBaseCurrency(params?: {
  baseCurrency?: string;
}): Promise<ExchangeRate[]> {
  return apiFetch<ExchangeRate[]>(`${CONSTANTS.routes.fxRatesApi}/${params?.baseCurrency ?? "USD"}`);
}