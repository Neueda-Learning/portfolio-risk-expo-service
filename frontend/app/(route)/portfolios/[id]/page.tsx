import { notFound } from "next/navigation";
import {
  getPortfolioId,
  getPortfolioStats,
  getPositionsFromPortfolioId,
} from "@/lib/api/portfolios";
import type { PortfolioOverview, PortfolioPositionRow } from "@/types";
import { PortfolioOverviewPage } from "@/components/portfolio/PortfolioOverviewPage";


export default async function PortfolioDetailsPage({
  params,
}: {
  params: Promise<{ id: string }> | { id: string };
}) {
  const { id } = await params;
  const portfolioId = Number(id);

  if (Number.isNaN(portfolioId)) {
    notFound();
  }

  const [portfolio, positions, stats] = await Promise.all([
    getPortfolioId(portfolioId),
    getPositionsFromPortfolioId(portfolioId),
    getPortfolioStats(portfolioId),
  ]);
  const details: PortfolioOverview = {
    ...portfolio,
    positions: positions.map((position: PortfolioPositionRow) => ({
      positionId: position.positionId,
      portfolioId: position.portfolioId,
      instrumentId: position.instrumentId,
      positionDate: position.positionDate,
      quantity: position.quantity,
      marketPrice: position.marketPrice,
      marketValue: position.marketValue,
      marketValueBase: position.marketValueBase,
      weightPct: position.weightPct,
      costBasis: position.costBasis,
      createdAt: position.createdAt,
      updatedAt: position.updatedAt,
      instrument: {
        instrumentId: position.instrumentId,
        instrumentName: position.instrumentName,
      },
    })),
  };

  return <PortfolioOverviewPage details={details} stats={stats} />;
}
