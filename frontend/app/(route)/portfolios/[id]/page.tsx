import { notFound } from "next/navigation";
import {
  getInstrumentFromPosition,
  getPortfolio,
  getPortfolioStats,
  getPositionsFromPortfolio,
} from "@/lib/api/portfolios";
import type { PortfolioOverview, PortfolioPositionRow } from "@/types";
import { PortfolioOverviewPage } from "@/components/portfolio/PortfolioOverviewPage";

function isNotFoundError(error: unknown): boolean {
  return error instanceof Error && error.message.includes(" 404 ");
}

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

  const [portfolio, stats] = await Promise.all([
    getPortfolio(portfolioId),
   getPortfolioStats(portfolioId),
  ]);

  let positions: PortfolioPositionRow[] = [];

  try {
   positions = await getPositionsFromPortfolio(portfolioId);
  } catch (error) {
   if (!isNotFoundError(error)) {
     throw error;
   }
  }

  const positionsWithInstrument = await Promise.all(
   positions.map(async (position: PortfolioPositionRow) => {
     const instrument = await getInstrumentFromPosition(
       portfolioId,
       position.positionId
     );

     return {
       ...position,
       instrument: instrument,
     };
   })
  );

  const details: PortfolioOverview = {
   ...portfolio,
   positions: positionsWithInstrument,
  };

  return <PortfolioOverviewPage details={details} stats={stats} />;
}
