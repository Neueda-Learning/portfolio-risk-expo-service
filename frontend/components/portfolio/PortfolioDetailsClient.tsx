"use client";

import Link from "next/link";
import { Search } from "lucide-react";
import { useMemo, useState } from "react";
import { formatCurrency } from "@/lib/format";
import type { PortfolioDetails, PortfolioPosition } from "@/lib/mock/portfolio-details";

function formatDecimal(value: number, maximumFractionDigits = 2): string {
  return new Intl.NumberFormat("en-GB", {
    maximumFractionDigits,
  }).format(value);
}

function formatPrice(value: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  }).format(value);
}

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

function DetailRow({
  label,
  value,
}: {
  label: string;
  value: string;
}) {
  return (
    <div className="rounded-md bg-gray-50 px-3 py-2">
      <p className="text-xs text-gray-400">{label}</p>
      <p className="mt-0.5 text-sm font-semibold text-gray-900">{value}</p>
    </div>
  );
}

export function PortfolioDetailsClient({
  details,
}: {
  details: PortfolioDetails;
}) {
  const [search, setSearch] = useState("");
  const [selectedPositionId, setSelectedPositionId] = useState(
    details.positions[0]?.positionId ?? null
  );

  const filteredPositions = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) {
      return details.positions;
    }

    return details.positions.filter((position) =>
      position.instrumentName.toLowerCase().includes(term)
    );
  }, [details.positions, search]);

  const selectedPosition: PortfolioPosition | null = useMemo(() => {
    return (
      details.positions.find((position) => position.positionId === selectedPositionId) ??
      filteredPositions[0] ??
      null
    );
  }, [details.positions, filteredPositions, selectedPositionId]);

  return (
    <div className="space-y-6">
      <section className="rounded-lg border-2 border-[#2660a6] bg-white p-5 shadow-sm">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[#2660a6]">
              Portfolio details
            </p>
            <h1 className="mt-1 truncate text-2xl font-bold text-gray-900">
              {details.portfolioName}
            </h1>
            <p className="mt-1 text-sm text-gray-500">
              {details.portfolioCode} · Managed by {details.manager} · As of{" "}
              {details.asOfDate}
            </p>
          </div>

          <Link
            href="/"
            className="rounded-md border border-[#2660a6] bg-white px-3 py-2 text-sm font-semibold text-[#2660a6] hover:bg-[#2660a6]/5"
          >
            Back to overview
          </Link>
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          <StatCard label="AUM" value={formatCurrency(details.aum, details.baseCurrency)} />
          <StatCard label="Base currency" value={details.baseCurrency} />
          <StatCard label="Positions" value={String(details.positions.length)} />
          <StatCard label="Benchmark" value={details.benchmark ?? "—"} />
        </div>
      </section>

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.6fr)_minmax(320px,0.9fr)]">
        <section className="min-w-0 rounded-lg border border-[#e5e7eb] bg-white shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[#e5e7eb] p-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Positions</h2>
              <p className="text-sm text-gray-500">
                Search by instrument name to filter the list.
              </p>
            </div>

            <div className="relative w-full max-w-sm">
              <Search
                className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400"
                aria-hidden="true"
              />
              <input
                type="search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search positions..."
                className="w-full rounded-md border border-gray-200 bg-white py-2 pl-9 pr-3 text-sm text-gray-900 outline-none focus:border-[#2660a6] focus:ring-1 focus:ring-[#2660a6]"
              />
            </div>
          </div>

          <div className="px-4 py-3 text-sm text-gray-500">
            Showing {filteredPositions.length} of {details.positions.length} positions
          </div>

          <div className="max-h-[560px] overflow-auto border-t border-[#e5e7eb]">
            <table className="min-w-full border-collapse text-left text-sm">
              <thead className="sticky top-0 z-10 bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3 font-semibold">Instrument</th>
                  <th className="px-4 py-3 font-semibold">ISIN</th>
                  <th className="px-4 py-3 font-semibold">Asset class</th>
                  <th className="px-4 py-3 font-semibold text-right">Weight</th>
                  <th className="px-4 py-3 font-semibold text-right">Market value</th>
                </tr>
              </thead>
              <tbody>
                {filteredPositions.length > 0 ? (
                  filteredPositions.map((position) => {
                    const isSelected = position.positionId === selectedPositionId;

                    return (
                      <tr
                        key={position.positionId}
                        className={isSelected ? "bg-[#2660a6]/5" : "hover:bg-gray-50"}
                      >
                        <td className="px-4 py-3">
                          <button
                            type="button"
                            onClick={() => setSelectedPositionId(position.positionId)}
                            className="text-left font-semibold text-[#2660a6] hover:underline"
                          >
                            {position.instrumentName}
                          </button>
                          <p className="mt-0.5 text-xs text-gray-400">{position.sector ?? "—"}</p>
                        </td>
                        <td className="px-4 py-3 font-mono text-xs text-gray-500">
                          {position.instrumentIsin}
                        </td>
                        <td className="px-4 py-3 text-gray-700">{position.assetClass}</td>
                        <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                          {formatDecimal(position.weightPct, 2)}%
                        </td>
                        <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                          {formatCurrency(position.marketValueBase, details.baseCurrency)}
                        </td>
                      </tr>
                    );
                  })
                ) : (
                  <tr>
                    <td colSpan={5} className="px-4 py-10 text-center text-sm text-gray-500">
                      No positions match your search.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        <aside className="min-w-0 rounded-lg border border-[#e5e7eb] bg-white shadow-sm">
          <div className="border-b border-[#e5e7eb] px-4 pt-4">
            <div className="inline-flex rounded-t-md border border-b-0 border-[#e5e7eb] bg-white">
              <button
                type="button"
                className="rounded-t-md border-b-2 border-[#2660a6] px-4 py-2 text-sm font-semibold text-[#2660a6]"
              >
                Instrument details
              </button>
            </div>
          </div>

          {selectedPosition ? (
            <div className="space-y-4 p-4">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">
                  Selected position
                </p>
                <h3 className="mt-1 text-lg font-bold text-gray-900">
                  {selectedPosition.instrumentName}
                </h3>
                <p className="text-sm text-gray-500">
                  Click a position instrument to switch the details tab.
                </p>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <DetailRow label="Instrument ID" value={String(selectedPosition.instrumentId)} />
                <DetailRow label="ISIN" value={selectedPosition.instrumentIsin} />
                <DetailRow label="Issuer" value={selectedPosition.issuer} />
                <DetailRow label="Asset class" value={selectedPosition.assetClass} />
                <DetailRow label="Sector" value={selectedPosition.sector ?? "—"} />
                <DetailRow label="Currency" value={selectedPosition.currency} />
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <DetailRow label="Quantity" value={formatDecimal(selectedPosition.quantity, 0)} />
                <DetailRow
                  label="Market price"
                  value={formatPrice(selectedPosition.marketPrice, selectedPosition.currency)}
                />
                <DetailRow
                  label="Market value"
                  value={formatCurrency(selectedPosition.marketValue, details.baseCurrency)}
                />
                <DetailRow label="Weight" value={`${formatDecimal(selectedPosition.weightPct, 2)}%`} />
              </div>

              <div className="rounded-md bg-gray-50 p-4 text-sm text-gray-600">
                <p className="font-semibold text-gray-900">Position snapshot</p>
                <p className="mt-1">
                  {selectedPosition.instrumentName} is the currently selected instrument.
                  The real instrument details view can replace this mock tab later.
                </p>
              </div>
            </div>
          ) : (
            <div className="p-4 text-sm text-gray-500">No instrument selected.</div>
          )}
        </aside>
      </div>
    </div>
  );
}
