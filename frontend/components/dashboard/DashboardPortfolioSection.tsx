"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { AlertTriangle, Search } from "lucide-react";
import { PortfolioCard } from "@/components/dashboard/PortfolioCard";
import { CONSTANTS } from "@/lib/constants";
import { STRINGS } from "@/lib/strings";
import type { Portfolio } from "@/types";

export function DashboardPortfolioSection({
  portfolios,
  breachCountByPortfolio,
  openBreachesCount,
}: {
  portfolios: Portfolio[];
  breachCountByPortfolio: Record<number, number>;
  openBreachesCount: number;
}) {
  const [search, setSearch] = useState("");

  const filteredPortfolios = useMemo(() => {
    const term = search.trim().toLowerCase();
    const filtered = term
      ? portfolios.filter((portfolio) =>
          portfolio.portfolioName.toLowerCase().includes(term)
        )
      : portfolios;

    return [...filtered].sort((a, b) =>
      a.portfolioName.localeCompare(b.portfolioName)
    );
  }, [portfolios, search]);

  return (
    <section className="min-w-0 flex-1">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-lg font-bold text-gray-900">
            {STRINGS.dashboard.title}
          </h1>
          <p className="text-sm text-gray-500">
            {filteredPortfolios.length}{" "}
            {filteredPortfolios.length === 1
              ? STRINGS.dashboard.activePortfolioSingular
              : STRINGS.dashboard.activePortfolioPlural}
          </p>
        </div>

        <div className="flex flex-1 items-center justify-end gap-3">
          <div className="relative w-full max-w-md">
            <Search
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400"
              aria-hidden="true"
            />
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder={STRINGS.dashboard.searchPlaceholder}
              className="w-full rounded-md border border-gray-200 bg-white py-2 pl-9 pr-3 text-sm text-gray-900 outline-none ring-1 ring-transparent focus:border-[#2660a6] focus:ring-[#2660a6]/20"
            />
          </div>
        </div>
      </div>

      <div className="flex flex-col gap-4">
        {filteredPortfolios.length > 0 ? (
          filteredPortfolios.map((portfolio) => (
            <PortfolioCard
              key={portfolio.portfolioId}
              portfolio={portfolio}
              openBreachCount={breachCountByPortfolio[portfolio.portfolioId] ?? 0}
            />
          ))
        ) : (
          <div className="rounded-lg border border-dashed border-gray-200 bg-white px-4 py-10 text-center text-sm text-gray-500">
            No portfolios found.
          </div>
        )}
      </div>
    </section>
  );
}
