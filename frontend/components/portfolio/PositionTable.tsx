import { STRINGS } from "@/lib/strings";
import { formatCurrency, formatDecimal } from "@/lib/format";
import type { PortfolioPositionWithInstrument } from "@/types";
import {
  PortfolioSortableHeader,
  PortfolioTable,
} from "@/components/ui/PortfolioTable";

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
    <PortfolioTable
      title={STRINGS.portfolioOverview.positions.title}
      summary={`${STRINGS.portfolioOverview.positions.showing} ${positions.length} ${STRINGS.portfolioOverview.positions.of} ${totalPositions} ${STRINGS.portfolioOverview.positions.suffix}`}
      searchValue={search}
      searchPlaceholder={STRINGS.portfolioOverview.positions.searchPlaceholder}
      onSearchChange={onSearchChange}
    >
      <div className="max-h-[560px] overflow-auto border-t border-[#e5e7eb]">
        <table className="min-w-full border-collapse text-left text-sm">
          <thead className="sticky top-0 z-10 bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
            <tr>
              <th className="px-4 py-3 text-left">
                <PortfolioSortableHeader
                  label={STRINGS.portfolioOverview.table.instrument}
                  isSorted={sortColumn === "instrument"}
                  sortDirection={sortDirection}
                  onClick={() => onSortClick("instrument")}
                />
              </th>
              <th className="px-4 py-3 text-left">
                <PortfolioSortableHeader
                  label={STRINGS.portfolioOverview.table.isin}
                  isSorted={sortColumn === "isin"}
                  sortDirection={sortDirection}
                  onClick={() => onSortClick("isin")}
                />
              </th>
              <th className="px-4 py-3 text-left">
                <PortfolioSortableHeader
                  label={STRINGS.portfolioOverview.table.assetClass}
                  isSorted={sortColumn === "assetClass"}
                  sortDirection={sortDirection}
                  onClick={() => onSortClick("assetClass")}
                />
              </th>
              <th className="px-4 py-3 text-right">
                <PortfolioSortableHeader
                  label={STRINGS.portfolioOverview.table.weight}
                  isSorted={sortColumn === "weight"}
                  sortDirection={sortDirection}
                  isNumeric={true}
                  onClick={() => onSortClick("weight")}
                />
              </th>
              <th className="px-4 py-3 text-right">
                <PortfolioSortableHeader
                  label={STRINGS.portfolioOverview.table.marketValue}
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
                          STRINGS.portfolioOverview.fallback.unknown}
                      </p>
                    </td>
                    <td className="px-4 py-3 font-mono text-xs text-gray-500">
                      {position.instrument.instrumentIsin ??
                        STRINGS.portfolioOverview.fallback.unknown}
                    </td>
                    <td className="px-4 py-3 text-gray-700">
                      {position.instrument.assetClass ??
                        STRINGS.portfolioOverview.fallback.unknown}
                    </td>
                    <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                      {formatDecimal(position.weightPct, 2)}%
                    </td>
                    <td className="px-4 py-3 text-right tabular-nums text-gray-700">
                      {formatCurrency(position.marketValueBase, baseCurrency)}
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
                  {STRINGS.portfolioOverview.positions.empty}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </PortfolioTable>
  );
}
