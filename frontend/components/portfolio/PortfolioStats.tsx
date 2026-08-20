import Link from "next/link";
import { CONSTANTS } from "@/lib/constants";
import { STRINGS } from "@/lib/strings";
import { formatCurrency } from "@/lib/format";
import type { PortfolioDetails } from "@/types";

function StatCard({
  label,
  value,
}: {
  label: string;
  value: string;
}) {
  return (
    <div className="rounded-md border border-[#e5e7eb] bg-white px-4 py-3">
      <p className="text-xs uppercase tracking-wide text-gray-400">{label}</p>
      <p className="mt-1 text-sm font-semibold text-gray-900">{value}</p>
    </div>
  );
}

export function PortfolioStats({ details }: { details: PortfolioDetails }) {
  return (
    <section className="rounded-lg border-2 border-[#2660a6] bg-white p-5 shadow-sm">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="min-w-0">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[#2660a6]">
            {STRINGS.portfolioDetails.title}
          </p>
          <h1 className="mt-1 truncate text-2xl font-bold text-gray-900">
            {details.portfolioName}
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            {details.portfolioCode} <br />
            {STRINGS.portfolioDetails.managedBy} {details.manager} <br />
          </p>
        </div>

        <Link
          href={CONSTANTS.routes.home}
          className="rounded-md border border-[#2660a6] bg-white px-3 py-2 text-sm font-semibold text-[#2660a6] hover:bg-[#2660a6]/5"
        >
          {STRINGS.portfolioDetails.backToOverview}
        </Link>
      </div>

      <div className="mt-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label={STRINGS.portfolioDetails.stats.aum}
          value={formatCurrency(details.aum, details.baseCurrency)}
        />
        <StatCard
          label={STRINGS.portfolioDetails.stats.baseCurrency}
          value={details.baseCurrency}
        />
        <StatCard
          label={STRINGS.portfolioDetails.stats.positionsCount}
          value={String(details.positions.length)}
        />
        <StatCard
          label={STRINGS.portfolioDetails.stats.benchmark}
          value={details.benchmark ?? STRINGS.portfolioDetails.fallback.unknown}
        />
      </div>
    </section>
  );
}
