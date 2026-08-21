"use client";

import { CheckCircle } from "lucide-react";
import { SeverityBadge } from "@/components/ui/SeverityBadge";
import { formatCurrency } from "@/lib/format";
import { STRINGS } from "@/lib/strings";
import type { LimitBreach } from "@/types";

interface BreachItemProps {
  breach: LimitBreach;
  onAccept: (breachId: number) => Promise<void>;
  isLoading?: boolean;
}

export function BreachItem({ breach, onAccept, isLoading }: BreachItemProps) {
  return (
    <div className="flex items-start justify-between gap-3 rounded-md border border-[#e5e7eb] bg-white p-3">
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
        onClick={() => onAccept(breach.breachId)}
        disabled={isLoading}
        className="shrink-0 inline-flex items-center gap-1.5 rounded-md bg-green-500 px-3 py-1.5 text-xs font-semibold text-white hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        aria-label={`${STRINGS.breaches.acceptButton} breach ${breach.breachId}`}
      >
        <CheckCircle className="h-3.5 w-3.5" aria-hidden="true" />
        {STRINGS.breaches.acceptButton}
      </button>
    </div>
  );
}
