export const CONSTANTS = {
  api: {
    baseUrl: "http://127.0.0.1:8080",
    revalidateSeconds: 30,
  },
  routes: {
    home: "/",
    breaches: "/breaches",
    breachesApi: "/api/limits/breaches",
    portfolios: "/api/portfolios",
  },
} as const;
