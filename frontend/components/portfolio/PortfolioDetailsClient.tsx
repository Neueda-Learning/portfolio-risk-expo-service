"use client";

import Link from "next/link";
import { Search, ChevronUp, ChevronDown } from "lucide-react";
import { useMemo, useState } from "react";
import { formatCurrency } from "@/lib/format";
import { CONSTANTS } from "@/lib/constants";
import { STRINGS } from "@/lib/strings";
import type { PortfolioDetails, PortfolioPositionWithInstrument } from "@/types";

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
  className = "",
}: {
  label: string;
  value: string;
  className?: string;
}) {
  return (
    <div className={`rounded-md bg-gray-50 px-3 py-2 ${className}`}>
      <p className="text-xs text-gray-400">{label}</p>
      <p className="mt-0.5 text-sm font-semibold text-gray-900">{value}</p>
    </div>
  );
}

function SortableHeader({
  label,
  isSorted,
  sortDirection,
  isNumeric = false,
  onClick,
}: {
  label: string;
  isSorted: boolean;
  sortDirection: "asc" | "desc";
  isNumeric?: boolean;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={`flex items-center gap-2 font-semibold hover:text-gray-700 ${isNumeric ? "justify-end" : ""}`}
    >
      {label}
      {isSorted && (
        <>
          {sortDirection === "asc" ? (
            <ChevronUp className="h-4 w-4 text-[#2660a6]" />
          ) : (
            <ChevronDown className="h-4 w-4 text-[#2660a6]" />
          )}
        </>
      )}
      {!isSorted && <span className="h-4 w-4" />}
    </button>
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
  const [sortColumn, setSortColumn] = useState<string>("instrument");
  const [sortDirection, setSortDirection] = useState<"asc" | "desc">("asc");

  const filteredAndSortedPositions = useMemo(() => {
    const term = search.trim().toLowerCase();
    let positions = details.positions;

    if (term) {
      positions = positions.filter((position) =>
        position.instrument.instrumentName.toLowerCase().includes(term)
      );
    }

    return [...positions].sort((a, b) => {
      let aVal: string | number = "";
      let bVal: string | number = "";

      switch (sortColumn) {
        case "instrument":
          aVal = a.instrument.instrumentName.toLowerCase();
          bVal = b.instrument.instrumentName.toLowerCase();
          break;
        case "isin":
          aVal = (a.instrument.instrumentIsin ?? "").toLowerCase();
          bVal = (b.instrument.instrumentIsin ?? "").toLowerCase();
          break;
        case "assetClass":
          aVal = (a.instrument.assetClass ?? "").toLowerCase();
          bVal = (b.instrument.assetClass ?? "").toLowerCase();
          break;
        case "weight":
          aVal = a.weightPct;
          bVal = b.weightPct;
          break;
        case "marketValue":
          aVal = a.marketValueBase;
          bVal = b.marketValueBase;
          break;
        default:
          return 0;
      }

      if (typeof aVal === "string" && typeof bVal === "string") {
        return sortDirection === "asc" ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);
      }

      const numA = Number(aVal);
      const numB = Number(bVal);
      return sortDirection === "asc" ? numA - numB : numB - numA;
    });
  }, [details.positions, search, sortColumn, sortDirection]);

  const handleSortClick = (column: string) => {
    if (sortColumn === column) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortColumn(column);
      setSortDirection("asc");
    }
  };

  const filteredPositions = filteredAndSortedPositions;

  const selectedPosition: PortfolioPositionWithInstrument | null = useMemo(() => {
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
              {STRINGS.portfolioDetails.title}
            </p>
            <h1 className="mt-1 truncate text-2xl font-bold text-gray-900">
              {details.portfolioName}
            </h1>
            <p className="mt-1 text-sm text-gray-500">
              {details.portfolioCode} <br/>
              {STRINGS.portfolioDetails.meta.managedBy}{" "}{details.manager} <br/>
              {/*{STRINGS.portfolioDetails.meta.asOf}{" "}{details.asOfDate}*/} //TODO: check if there is a date
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
          <StatCard label={STRINGS.portfolioDetails.stats.aum} value={formatCurrency(details.aum, details.baseCurrency)} />
          <StatCard label={STRINGS.portfolioDetails.stats.baseCurrency} value={details.baseCurrency} />
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

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.6fr)_minmax(320px,0.9fr)]">
        <section className="min-w-0 rounded-lg border border-[#e5e7eb] bg-white shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[#e5e7eb] p-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">
                {STRINGS.portfolioDetails.positions.title}
              </h2>
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
                placeholder={STRINGS.portfolioDetails.positions.searchPlaceholder}
                className="w-full rounded-md border border-gray-200 bg-white py-2 pl-9 pr-3 text-sm text-gray-900 outline-none focus:border-[#2660a6] focus:ring-1 focus:ring-[#2660a6]"
              />
            </div>
          </div>

          <div className="px-4 py-3 text-sm text-gray-500">
            {STRINGS.portfolioDetails.positions.showing} {filteredPositions.length}{" "}
            {STRINGS.portfolioDetails.positions.of} {details.positions.length}{" "}
            {STRINGS.portfolioDetails.positions.suffix}
          </div>

          <div className="max-h-[560px] overflow-auto border-t border-[#e5e7eb]">
            <table className="min-w-full border-collapse text-left text-sm">
              <thead className="sticky top-0 z-10 bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3 text-left">
                    <SortableHeader
                      label={STRINGS.portfolioDetails.table.instrument}
                      isSorted={sortColumn === "instrument"}
                      sortDirection={sortDirection}
                      onClick={() => handleSortClick("instrument")}
                    />
                  </th>
                  <th className="px-4 py-3 text-left">
                    <SortableHeader
                      label={STRINGS.portfolioDetails.table.isin}
                      isSorted={sortColumn === "isin"}
                      sortDirection={sortDirection}
                      onClick={() => handleSortClick("isin")}
                    />
                  </th>
                  <th className="px-4 py-3 text-left">
                    <SortableHeader
                      label={STRINGS.portfolioDetails.table.assetClass}
                      isSorted={sortColumn === "assetClass"}
                      sortDirection={sortDirection}
                      onClick={() => handleSortClick("assetClass")}
                    />
                  </th>
                  <th className="px-4 py-3 text-right">
                    <SortableHeader
                      label={STRINGS.portfolioDetails.table.weight}
                      isSorted={sortColumn === "weight"}
                      sortDirection={sortDirection}
                      isNumeric={true}
                      onClick={() => handleSortClick("weight")}
                    />
                  </th>
                  <th className="px-4 py-3 text-right">
                    <SortableHeader
                      label={STRINGS.portfolioDetails.table.marketValue}
                      isSorted={sortColumn === "marketValue"}
                      sortDirection={sortDirection}
                      isNumeric={true}
                      onClick={() => handleSortClick("marketValue")}
                    />
                  </th>
                </tr>
              </thead>
              <tbody>
                {filteredPositions.length > 0 ? (
                  filteredPositions.map((position) => {
                    const isSelected = position.positionId === selectedPositionId;

                    return (
                      <tr
                        key={position.positionId}
                        onClick={() => setSelectedPositionId(position.positionId)}
                        onKeyDown={(event) => {
                          if (event.key === "Enter" || event.key === " ") {
                            event.preventDefault();
                            setSelectedPositionId(position.positionId);
                          }
                        }}
                        tabIndex={0}
                        role="button"
                        aria-label={`Select ${position.instrument.instrumentName}`}
                        className={`cursor-pointer ${
                          isSelected ? "bg-[#2660a6]/5" : "hover:bg-gray-50"
                        }`}
                      >
                        <td className="px-4 py-3">
                          <span className="font-semibold text-[#2660a6]">
                            {position.instrument.instrumentName}
                          </span>
                          <p className="mt-0.5 text-xs text-gray-400">
                            {position.instrument.sector ?? STRINGS.portfolioDetails.fallback.unknown}
                          </p>
                        </td>
                        <td className="px-4 py-3 font-mono text-xs text-gray-500">
                          {position.instrument.instrumentIsin ?? STRINGS.portfolioDetails.fallback.unknown}
                        </td>
                        <td className="px-4 py-3 text-gray-700">
                          {position.instrument.assetClass ?? STRINGS.portfolioDetails.fallback.unknown}
                        </td>
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
                      {STRINGS.portfolioDetails.positions.empty}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        <aside className="min-w-0 rounded-lg border border-[#e5e7eb] bg-white shadow-sm">
          <div className="px-4 pt-4">
            <div className="inline-flex border-b-2 border-[#2660a6] px-1 pb-2 text-sm font-semibold text-[#2660a6]">
              {STRINGS.portfolioDetails.details.tabTitle}
            </div>
          </div>

          {selectedPosition ? (
            <div className="space-y-4 p-4">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">
                  {STRINGS.portfolioDetails.details.selectedPosition}
                </p>
                <h3 className="mt-1 text-lg font-bold text-gray-900">
                  {selectedPosition.instrument.instrumentName}
                </h3>
              </div>


              <div className="grid gap-3 sm:grid-cols-2">
                <DetailRow
                  label={STRINGS.portfolioDetails.labels.isin}
                  value={selectedPosition.instrument.instrumentIsin ?? STRINGS.portfolioDetails.fallback.unknown}
                  className="sm:col-span-2"
                />
                <DetailRow
                  label={STRINGS.portfolioDetails.labels.issuer}
                  value={selectedPosition.instrument.issuer ?? STRINGS.portfolioDetails.fallback.unknown}
                />
                <DetailRow
                  label={STRINGS.portfolioDetails.labels.assetClass}
                  value={selectedPosition.instrument.assetClass ?? STRINGS.portfolioDetails.fallback.unknown}
                />
                <DetailRow
                  label={STRINGS.portfolioDetails.labels.sector}
                  value={selectedPosition.instrument.sector ?? STRINGS.portfolioDetails.fallback.unknown}
                />
                <DetailRow
                  label={STRINGS.portfolioDetails.labels.currency}
                  value={selectedPosition.instrument.currency ?? details.baseCurrency}
                />
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <DetailRow
                  label={STRINGS.portfolioDetails.labels.quantity}
                  value={formatDecimal(selectedPosition.quantity, 0)}
                />
                <DetailRow
                  label={STRINGS.portfolioDetails.labels.marketPrice}
                  value={formatPrice(selectedPosition.marketPrice, selectedPosition.instrument.currency ?? details.baseCurrency)}
                />
                <DetailRow
                  label={STRINGS.portfolioDetails.labels.marketValue}
                  value={formatCurrency(selectedPosition.marketValue, details.baseCurrency)}
                />
                <DetailRow
                  label={STRINGS.portfolioDetails.labels.weight}
                  value={`${formatDecimal(selectedPosition.weightPct, 2)}%`}
                />
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
