// Always render at request time - data must come from the live API
export const dynamic = "force-dynamic";

import Link from "next/link";
import { AlertTriangle } from "lucide-react";
import { getPortfolios, getOpenBreaches } from "@/lib/api";
import { PortfolioCard } from "@/components/dashboard/PortfolioCard";
import { GlobalAlertsPanel } from "@/components/dashboard/GlobalAlertsPanel";

export default async function DashboardPage() {
  const [portfolios, openBreaches] = await Promise.all([
    getPortfolios(),
    getOpenBreaches(),
  ]);

  const breachCountByPortfolio = openBreaches.reduce<Record<number, number>>(
    (acc, b) => {
      acc[b.portfolioId] = (acc[b.portfolioId] ?? 0) + 1;
      return acc;
    },
    {}
  );

  const activePortfolios = portfolios.filter((p) => p.isActive);

  return (
    <div className="flex flex-col gap-6 lg:flex-row lg:items-start">
      <section className="min-w-0 flex-1">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h1 className="text-lg font-bold text-gray-900">Portfolio Overview</h1>
            <p className="text-sm text-gray-500">
              {activePortfolios.length} active portfolio
              {activePortfolios.length !== 1 ? "s" : ""}
            </p>
          </div>
          <div className="hidden sm:flex items-center gap-2">
            {openBreaches.length > 0 && (
              <Link
                href="/breaches?status=OPEN"
                className="flex items-center gap-1.5 rounded-full border-2 border-[#2660a6] bg-white px-3 py-1 text-xs font-semibold text-[#db0011] shadow-md hover:bg-[#2660a6]/5"
              >
                <AlertTriangle className="h-3.5 w-3.5" />
                {openBreaches.length} open breach
                {openBreaches.length !== 1 ? "es" : ""}
              </Link>
            )}
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
          {activePortfolios.map((portfolio) => (
            <PortfolioCard
              key={portfolio.portfolioId}
              portfolio={portfolio}
              openBreachCount={breachCountByPortfolio[portfolio.portfolioId] ?? 0}
            />
          ))}
        </div>
      </section>

      <GlobalAlertsPanel breaches={openBreaches} />
    </div>
  );
}
