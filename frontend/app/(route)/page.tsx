// Always render at request time - data must come from the live API
export const dynamic = "force-dynamic";

import { getPortfolios, getOpenBreaches } from "@/lib/api";
import { DashboardPortfolioSection } from "@/components/dashboard/DashboardPortfolioSection";
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
      <DashboardPortfolioSection
        portfolios={activePortfolios}
        breachCountByPortfolio={breachCountByPortfolio}
      />

      <GlobalAlertsPanel breaches={openBreaches} />
    </div>
  );
}
