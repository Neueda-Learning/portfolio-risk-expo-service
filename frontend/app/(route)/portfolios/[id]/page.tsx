import { notFound } from "next/navigation";
import { PortfolioDetailsClient } from "@/components/portfolio/PortfolioDetailsClient";
import {getPortfolioId, getPositionsFromPortfolioId} from "@/lib/api/portfolios";
import type { PortfolioDetails, PortfolioPositionApiRow } from "@/types";


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

  const portfolio = await getPortfolioId(portfolioId);
  const positions = await getPositionsFromPortfolioId(portfolio.portfolioId);
  const details: PortfolioDetails = {
    ...portfolio,
    positions: positions.map((position: PortfolioPositionApiRow) => ({
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

  return <PortfolioDetailsClient details={details} />;
}
