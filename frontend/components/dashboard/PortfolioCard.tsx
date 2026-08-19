import Link from "next/link";
import { AlertTriangle, ChevronRight, Users } from "lucide-react";
import { PortfolioTypeBadge } from "@/components/ui/PortfolioTypeBadge";
import { MetricCell } from "@/components/ui/MetricCell";
import { formatCurrency } from "@/lib/format";
import type { Portfolio } from "@/types";

export function PortfolioCard({
  portfolio,
  openBreachCount,
}: {
  portfolio: Portfolio;
  openBreachCount: number;
}) {
  return (
    <Link
      href={`/frontend/app/(route)/portfolios/${portfolio.portfolioId}`}
      className="group flex flex-col rounded-lg border border-[#e5e7eb] bg-white shadow-sm transition hover:shadow-md hover:border-[#2660a6]/40"
    >
      {/* Card header */}
      <div className="flex items-start justify-between gap-2 p-4 pb-3">
        <div className="min-w-0">
          <p className="truncate text-xs font-mono text-gray-400">
            {portfolio.portfolioCode}
          </p>
          <h2 className="mt-0.5 truncate text-sm font-semibold text-gray-900 group-hover:text-[#2660a6]">
            {portfolio.portfolioName}
          </h2>
        </div>
        <PortfolioTypeBadge type={portfolio.portfolioType} />
      </div>

      {/* Metrics */}
      <div className="grid grid-cols-2 gap-px border-t border-[#e5e7eb] bg-[#e5e7eb]">
        <MetricCell
          label="AUM"
          value={formatCurrency(portfolio.aum, portfolio.baseCurrency)}
        />
        <MetricCell label="Currency" value={portfolio.baseCurrency} />
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between gap-2 p-3 pt-2">
        <span className="flex items-center gap-1 text-xs text-gray-500">
          <Users className="h-3.5 w-3.5" aria-hidden="true" />
          {portfolio.manager}
        </span>
        <div className="flex items-center gap-2">
          {openBreachCount > 0 && (
            <span className="flex items-center gap-1 rounded-full bg-[#db0011]/10 px-2 py-0.5 text-xs font-semibold text-[#db0011]">
              <AlertTriangle className="h-3 w-3" aria-hidden="true" />
              {openBreachCount} breach{openBreachCount > 1 ? "es" : ""}
            </span>
          )}
          <ChevronRight className="h-4 w-4 text-gray-300 group-hover:text-[#2660a6]" />
        </div>
      </div>
    </Link>
  );
}
