"use client";

import { STRINGS } from "@/lib/strings";
import { formatCurrency, formatDecimal } from "@/lib/format";
import type { PortfolioStatsData } from "@/types";
import { PortfolioTable } from "@/components/shared/PortfolioTable";

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

function toSortedEntries(values: Record<string, number>) {
  return Object.entries(values).sort(([, a], [, b]) => b - a);
}

function getLimitStatusColour(status: string) {
  switch (status) {
    case "WARNING":
      return "border-yellow-200 bg-yellow-50 text-yellow-700";
    case "BREACH":
      return "border-red-200 bg-red-50 text-red-700";
    case "OK":
      return "border-green-200 bg-green-50 text-green-700";
    default:
      return "border-gray-200 bg-gray-50 text-gray-700";
  }
}

export function PortfolioStats({ stats }: { stats: PortfolioStatsData }) {
  const baseCurrency =
    stats.limits.baseCurrency || stats.exposure.currency;
  const totalExposure = stats.exposure.totalExposure || stats.limits.totalExposure;
  const breachedLimits = stats.limits.limits.filter((limit) => limit.isBreached).length;
  const hasBreaches = breachedLimits > 0;
  const sectorEntries = toSortedEntries(stats.sectorExposure.sectorExposures);
  const assetEntries = toSortedEntries(stats.assetExposure.assetExposures);
  const snapshotTone = hasBreaches ? "red" : "green";

  return (
    <div className="space-y-6">
      <section
        className="rounded-lg border-2 bg-white p-5 shadow-sm"
        style={{ borderColor: snapshotTone }}
      >
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0">
            <p
              className="text-xs font-semibold uppercase tracking-[0.2em]"
              style={{ color: snapshotTone }}
            >
              {STRINGS.portfolioOverview.stats.title}
            </p>
          </div>
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          <StatCard
            label={STRINGS.portfolioOverview.stats.exposure}
            value={formatCurrency(totalExposure, baseCurrency)}
          />
          <StatCard
            label={STRINGS.portfolioOverview.stats.var1Day}
            value={
              stats.var
                ? formatCurrency(stats.var.var1Day, stats.var.currency)
                : STRINGS.portfolioOverview.stats.empty
            }
          />
          <StatCard
            label={STRINGS.portfolioOverview.stats.limitsCount}
            value={String(stats.limits.limits.length)}
          />
          <StatCard
            label={STRINGS.portfolioOverview.stats.breachedLimits}
            value={`${breachedLimits} / ${stats.limits.limits.length}`}
          />
        </div>
      </section>

      <div className="grid gap-6 xl:grid-cols-2">
        <PortfolioTable title={STRINGS.portfolioOverview.stats.bySector}>
          <div className="max-h-[360px] overflow-auto border-t border-[#e5e7eb]">
            <table className="min-w-full border-collapse text-left text-sm">
              <thead className="sticky top-0 z-10 bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3 text-left">
                    {STRINGS.portfolioOverview.stats.sector}
                  </th>
                  <th className="px-4 py-3 text-right">
                    {STRINGS.portfolioOverview.stats.exposure}
                  </th>
                  <th className="px-4 py-3 text-right">
                    {STRINGS.portfolioOverview.stats.share}
                  </th>
                </tr>
              </thead>
              <tbody>
                {sectorEntries.length > 0 ? (
                  sectorEntries.map(([sector, amount]) => (
                    <tr key={sector}>
                      <td className="px-4 py-3 font-medium text-gray-800">{sector}</td>
                      <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                        {formatCurrency(amount, baseCurrency)}
                      </td>
                      <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                        {totalExposure > 0
                          ? `${formatDecimal((amount / totalExposure) * 100, 1)}%`
                          : "0%"}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={3} className="px-4 py-8 text-center text-sm text-gray-500">
                      {STRINGS.portfolioOverview.stats.empty}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </PortfolioTable>

        <PortfolioTable title={STRINGS.portfolioOverview.stats.byAsset}>
          <div className="max-h-[360px] overflow-auto border-t border-[#e5e7eb]">
            <table className="min-w-full border-collapse text-left text-sm">
              <thead className="sticky top-0 z-10 bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3 text-left">
                    {STRINGS.portfolioOverview.table.assetClass}
                  </th>
                  <th className="px-4 py-3 text-right">
                    {STRINGS.portfolioOverview.stats.exposure}
                  </th>
                  <th className="px-4 py-3 text-right">
                    {STRINGS.portfolioOverview.stats.share}
                  </th>
                </tr>
              </thead>
              <tbody>
                {assetEntries.length > 0 ? (
                  assetEntries.map(([assetClass, amount]) => (
                    <tr key={assetClass}>
                      <td className="px-4 py-3 font-medium text-gray-800">{assetClass}</td>
                      <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                        {formatCurrency(amount, baseCurrency)}
                      </td>
                      <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                        {totalExposure > 0
                          ? `${formatDecimal((amount / totalExposure) * 100, 1)}%`
                          : "0%"}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={3} className="px-4 py-8 text-center text-sm text-gray-500">
                      {STRINGS.portfolioOverview.stats.empty}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </PortfolioTable>
      </div>

      <PortfolioTable title={STRINGS.portfolioOverview.stats.limitsTitle}>
        <div className="max-h-[420px] overflow-auto border-t border-[#e5e7eb]">
          <table className="min-w-full border-collapse text-left text-sm">
            <thead className="sticky top-0 z-10 bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
              <tr>
                <th className="px-4 py-3 text-left">
                  {STRINGS.portfolioOverview.stats.limitType}
                </th>
                <th className="px-4 py-3 text-right">
                  {STRINGS.portfolioOverview.stats.currentValue}
                </th>
                <th className="px-4 py-3 text-right">
                  {STRINGS.portfolioOverview.stats.limitValue}
                </th>
                <th className="px-4 py-3 text-right">
                  {STRINGS.portfolioOverview.stats.utilisation}
                </th>
                <th className="px-4 py-3 text-right">
                  {STRINGS.portfolioOverview.stats.status}
                </th>
              </tr>
            </thead>
            <tbody>
              {stats.limits.limits.length > 0 ? (
                stats.limits.limits.map((limit) => {
                  const colour = getLimitStatusColour(limit.status);

                  return (
                  <tr key={limit.limitId}>
                    <td className="px-4 py-3 font-medium text-gray-800">
                      {limit.limitType}
                    </td>
                    <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                      {formatCurrency(limit.currentValue, baseCurrency)}
                    </td>
                    <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                      {formatCurrency(limit.limitValue, baseCurrency)}
                    </td>
                    <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                      {formatDecimal(limit.utilisationPct, 1)}%
                    </td>
                    <td className="px-4 py-3 text-right">
                      <span
                        className={`inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-medium ${colour}`}
                      >
                        {limit.status}
                      </span>
                    </td>
                  </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-sm text-gray-500">
                    {STRINGS.portfolioOverview.stats.empty}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </PortfolioTable>
    </div>
  );
}
