import { Search, ChevronUp, ChevronDown } from "lucide-react";
import { STRINGS } from "@/lib/strings";
import { formatCurrency, formatDecimal} from "@/lib/format";
import type { PortfolioPositionWithInstrument } from "@/types";

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
      className={`flex items-center gap-2 font-semibold hover:text-gray-700 ${
        isNumeric ? "justify-end" : ""
      }`}
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

interface PositionTableProps {
  positions: PortfolioPositionWithInstrument[];
  search: string;
  onSearchChange: (value: string) => void;
  sortColumn: string;
  sortDirection: "asc" | "desc";
  onSortClick: (column: string) => void;
  selectedPositionId: number | null;
  onPositionSelect: (positionId: number) => void;
  baseCurrency: string;
  totalPositions: number;
}

export function PositionTable({
  positions,
  search,
  onSearchChange,
  sortColumn,
  sortDirection,
  onSortClick,
  selectedPositionId,
  onPositionSelect,
  baseCurrency,
  totalPositions,
}: PositionTableProps) {
  return (
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
            onChange={(event) => onSearchChange(event.target.value)}
            placeholder={STRINGS.portfolioDetails.positions.searchPlaceholder}
            className="w-full rounded-md border border-gray-200 bg-white py-2 pl-9 pr-3 text-sm text-gray-900 outline-none focus:border-[#2660a6] focus:ring-1 focus:ring-[#2660a6]"
          />
        </div>
      </div>

      <div className="px-4 py-3 text-sm text-gray-500">
        {STRINGS.portfolioDetails.positions.showing} {positions.length}{" "}
        {STRINGS.portfolioDetails.positions.of} {totalPositions}{" "}
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
                  onClick={() => onSortClick("instrument")}
                />
              </th>
              <th className="px-4 py-3 text-left">
                <SortableHeader
                  label={STRINGS.portfolioDetails.table.isin}
                  isSorted={sortColumn === "isin"}
                  sortDirection={sortDirection}
                  onClick={() => onSortClick("isin")}
                />
              </th>
              <th className="px-4 py-3 text-left">
                <SortableHeader
                  label={STRINGS.portfolioDetails.table.assetClass}
                  isSorted={sortColumn === "assetClass"}
                  sortDirection={sortDirection}
                  onClick={() => onSortClick("assetClass")}
                />
              </th>
              <th className="px-4 py-3 text-right">
                <SortableHeader
                  label={STRINGS.portfolioDetails.table.weight}
                  isSorted={sortColumn === "weight"}
                  sortDirection={sortDirection}
                  isNumeric={true}
                  onClick={() => onSortClick("weight")}
                />
              </th>
              <th className="px-4 py-3 text-right">
                <SortableHeader
                  label={STRINGS.portfolioDetails.table.marketValue}
                  isSorted={sortColumn === "marketValue"}
                  sortDirection={sortDirection}
                  isNumeric={true}
                  onClick={() => onSortClick("marketValue")}
                />
              </th>
            </tr>
          </thead>
          <tbody>
            {positions.length > 0 ? (
              positions.map((position) => {
                const isSelected = position.positionId === selectedPositionId;

                return (
                  <tr
                    key={position.positionId}
                    onClick={() => onPositionSelect(position.positionId)}
                    onKeyDown={(event) => {
                      if (event.key === "Enter" || event.key === " ") {
                        event.preventDefault();
                        onPositionSelect(position.positionId);
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
                        {position.instrument.sector ??
                          STRINGS.portfolioDetails.fallback.unknown}
                      </p>
                    </td>
                    <td className="px-4 py-3 font-mono text-xs text-gray-500">
                      {position.instrument.instrumentIsin ??
                        STRINGS.portfolioDetails.fallback.unknown}
                    </td>
                    <td className="px-4 py-3 text-gray-700">
                      {position.instrument.assetClass ??
                        STRINGS.portfolioDetails.fallback.unknown}
                    </td>
                    <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                      {formatDecimal(position.weightPct, 2)}%
                    </td>
                    <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                      {formatCurrency(
                        position.marketValueBase,
                        baseCurrency
                      )}
                    </td>
                  </tr>
                );
              })
            ) : (
              <tr>
                <td
                  colSpan={5}
                  className="px-4 py-10 text-center text-sm text-gray-500"
                >
                  {STRINGS.portfolioDetails.positions.empty}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
