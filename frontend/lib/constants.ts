export const CONSTANTS = {
  api: {
    baseUrl: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080",
    backendUrl: process.env.BACKEND_URL || "http://backend:8080",
    revalidateSeconds: 30,
  },
  routes: {
    home: "/",
    breaches: "/breaches",
    breachesApi: "/api/limits/breaches",
    portfolios: "/api/portfolios",
    fxRatesApi: "/api/fx-rates",
  },
} as const;
