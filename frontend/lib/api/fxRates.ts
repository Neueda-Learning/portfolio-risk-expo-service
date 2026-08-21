import { apiFetch } from "./fetch";
import { CONSTANTS } from "@/lib/constants";
import type { ExchangeRate } from "@/types";

export async function getExchangeRates(): Promise<ExchangeRate[]> {
  return apiFetch<ExchangeRate[]>(CONSTANTS.routes.fxRatesApi);
}

export async function getExchangeRatesForBaseCurrency(params?: {
  baseCurrency?: string;
}): Promise<ExchangeRate[]> {
  return apiFetch<ExchangeRate[]>(`${CONSTANTS.routes.fxRatesApi}/${params?.baseCurrency ?? "USD"}`);
}