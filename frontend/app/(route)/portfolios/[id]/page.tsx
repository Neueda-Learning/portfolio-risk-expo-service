import { notFound } from "next/navigation";
import { PortfolioDetailsClient } from "@/components/portfolio/PortfolioDetailsClient";
import {getPortfolioId, getPositionsFromPortfolioId} from "@/lib/api/portfolios";


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
  const details = await getPositionsFromPortfolioId(portfolio.portfolioId);

  return <PortfolioDetailsClient details={details} />;
}
