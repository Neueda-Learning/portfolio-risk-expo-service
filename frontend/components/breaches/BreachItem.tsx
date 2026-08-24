"use client";

import { SeverityBadge } from "@/components/shared/SeverityBadge";
import { formatCurrency } from "@/lib/format";
import { STRINGS } from "@/lib/strings";
import type { LimitBreach } from "@/types";

interface BreachItemProps {
  breach: LimitBreach;
  onAccept: (breachId: number) => Promise<void>;
  isLoading?: boolean;
  isAcknowledged?: boolean;
}

export function BreachItem({
  breach,
  onAccept,
  isLoading,
  isAcknowledged = false,
}: BreachItemProps) {
  const buttonLabel = isAcknowledged ? STRINGS.breaches.acceptedButton : STRINGS.breaches.acceptButton;
  const buttonClassName = isAcknowledged
    ? "shrink-0 rounded-md bg-green-600 px-3 py-1.5 text-xs font-semibold text-white transition-colors cursor-default"
    : "shrink-0 gap-1.5 rounded-md bg-green-200 px-3 py-1.5 text-xs font-semibold text-green-900 hover:bg-green-300 disabled:cursor-not-allowed disabled:opacity-50 transition-colors";

  return (
    <div className="flex items-center justify-between gap-3 rounded-md border border-[#e5e7eb] bg-white p-3">
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <SeverityBadge severity={breach.severity} />
          <span className="text-xs font-medium text-gray-700">{breach.limitType}</span>
        </div>
        <p className="mt-2 text-xs text-gray-500">
          {STRINGS.alerts.excess}:{" "}
          <span className="font-semibold text-[#db0011]">
            {formatCurrency(breach.excessAmount, "GBP")}
          </span>
          {" "}| {breach.breachDate}
        </p>
      </div>
      <button
        onClick={isAcknowledged ? undefined : () => onAccept(breach.breachId)}
        disabled={isLoading || isAcknowledged}
        className={buttonClassName}
        aria-label={`${buttonLabel} breach ${breach.breachId}`}
      >
        {buttonLabel}
      </button>
    </div>
  );
}
