import Link from "next/link";
import { AlertCircle } from "lucide-react";
import { SeverityBadge } from "@/components/shared/SeverityBadge";
import { formatCurrency } from "@/lib/format";
import { CONSTANTS } from "@/lib/constants";
import { STRINGS } from "@/lib/strings";
import type { LimitBreach } from "@/types";

export function BreachRow({ breach }: { breach: LimitBreach }) {
  return (
    <div className="flex items-start gap-3 rounded-md border border-[#e5e7eb] bg-white p-3 shadow-sm">
      <AlertCircle
        className="mt-0.5 h-4 w-4 shrink-0 text-[#db0011]"
        aria-hidden="true"
      />
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-1.5">
          <SeverityBadge severity={breach.severity} />
          {breach.portfolioName && (
            <span className="truncate text-xs font-medium text-gray-700">
              {breach.portfolioName}
            </span>
          )}
        </div>
        <p className="mt-1 text-xs text-gray-500">
          {STRINGS.alerts.excess}:{" "}
          <span className="font-semibold text-[#db0011]">
            {formatCurrency(breach.excessAmount, "GBP")}
          </span>{" "}
           | {breach.breachDate}
        </p>
        {breach.limitType && (
          <p className="text-xs text-gray-400">{breach.limitType}</p>
        )}
      </div>
      <Link
          href={`${CONSTANTS.routes.home}portfolios/${breach.portfolioId}`}
          className="shrink-0 text-xs text-[#2660a6] hover:underline"
      >
        {STRINGS.alerts.view}
      </Link>
    </div>
  );
}
