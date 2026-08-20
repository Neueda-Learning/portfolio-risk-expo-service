import { STRINGS } from "@/lib/strings";
import { formatCurrency, formatDecimal, formatPrice } from "@/lib/format";
import type { PortfolioPositionWithInstrument } from "@/types";

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

interface PositionDetailsProps {
  selectedPosition: PortfolioPositionWithInstrument | null;
  baseCurrency: string;
}

export function PositionDetails({
  selectedPosition,
  baseCurrency,
}: PositionDetailsProps) {
  return (
    <aside className="min-w-0 rounded-lg border border-[#e5e7eb] bg-white shadow-sm">
      <div className="px-4 pt-4">
        <div className="inline-flex border-b-2 border-[#2660a6] px-1 pb-2 text-sm font-semibold text-[#2660a6]">
          {STRINGS.portfolioOverview.instrumentDetails.tabTitle}
        </div>
      </div>

      {selectedPosition ? (
        <div className="space-y-4 p-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">
              {STRINGS.portfolioOverview.instrumentDetails.selectedPosition}
            </p>
            <h3 className="mt-1 text-lg font-bold text-gray-900">
              {selectedPosition.instrument.instrumentName}
            </h3>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <DetailRow
              label={STRINGS.portfolioOverview.labels.isin}
              value={
                selectedPosition.instrument.instrumentIsin ??
                STRINGS.portfolioOverview.fallback.unknown
              }
              className="sm:col-span-2"
            />
            <DetailRow
              label={STRINGS.portfolioOverview.labels.issuer}
              value={
                selectedPosition.instrument.issuer ??
                STRINGS.portfolioOverview.fallback.unknown
              }
            />
            <DetailRow
              label={STRINGS.portfolioOverview.labels.assetClass}
              value={
                selectedPosition.instrument.assetClass ??
                STRINGS.portfolioOverview.fallback.unknown
              }
            />
            <DetailRow
              label={STRINGS.portfolioOverview.labels.sector}
              value={
                selectedPosition.instrument.sector ??
                STRINGS.portfolioOverview.fallback.unknown
              }
            />
            <DetailRow
              label={STRINGS.portfolioOverview.labels.currency}
              value={selectedPosition.instrument.currency ?? baseCurrency}
            />
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <DetailRow
              label={STRINGS.portfolioOverview.labels.quantity}
              value={formatDecimal(selectedPosition.quantity, 0)}
            />
            <DetailRow
              label={STRINGS.portfolioOverview.labels.marketPrice}
              value={formatPrice(
                selectedPosition.marketPrice,
                selectedPosition.instrument.currency ?? baseCurrency
              )}
            />
            <DetailRow
              label={STRINGS.portfolioOverview.labels.marketValue}
              value={formatCurrency(
                selectedPosition.marketValue,
                baseCurrency
              )}
            />
            <DetailRow
              label={STRINGS.portfolioOverview.labels.weight}
              value={`${formatDecimal(selectedPosition.weightPct, 2)}%`}
            />
          </div>
        </div>
      ) : (
        <div className="p-4 text-sm text-gray-500">No instrument selected.</div>
      )}
    </aside>
  );
}
