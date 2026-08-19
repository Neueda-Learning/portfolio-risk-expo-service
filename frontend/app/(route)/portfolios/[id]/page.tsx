import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { PortfolioDetailsClient } from "@/components/portfolio/PortfolioDetailsClient";
import { getMockPortfolioDetails } from "@/lib/mock/portfolio-details";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }> | { id: string };
}): Promise<Metadata> {
  const { id } = await params;
  const portfolioId = Number(id);
  if (Number.isNaN(portfolioId)) {
    return {
      title: "Portfolio details",
    };
  }

  const details = getMockPortfolioDetails(portfolioId);
  return {
    title: `${details.portfolioName} | Portfolio details`,
  };
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

  const details = getMockPortfolioDetails(portfolioId);

  return <PortfolioDetailsClient details={details} />;
}
