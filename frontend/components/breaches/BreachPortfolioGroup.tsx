"use client";

import { useState } from "react";
import { ChevronDown, ChevronUp } from "lucide-react";
import Link from "next/link";
import { BreachItem } from "./BreachItem";
import { CONSTANTS } from "@/lib/constants";
import { STRINGS } from "@/lib/strings";
import type { LimitBreach, Portfolio } from "@/types";

interface BreachPortfolioGroupProps {
  portfolio: Portfolio;
  breaches: LimitBreach[];
  onBreachAccepted: (breachId: number) => Promise<void>;
  loadingBreachIds?: Set<number>;
}

export function BreachPortfolioGroup({
  portfolio,
  breaches,
  onBreachAccepted,
  loadingBreachIds = new Set(),
}: BreachPortfolioGroupProps) {
  const [isExpanded, setIsExpanded] = useState(false);

  return (
    <div className="w-full rounded-lg border border-[#e5e7eb] bg-white shadow-sm overflow-hidden">
      {/* Portfolio header */}
      <button
        type="button"
        onClick={() => setIsExpanded(!isExpanded)}
        className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left transition-colors hover:bg-gray-50"
      >
        <div className="min-w-0 flex-1">
          <p className="text-xs font-mono text-gray-400">{portfolio.portfolioCode}</p>
          <h3 className="mt-0.5 truncate text-sm font-semibold text-gray-900">
            <Link
              href={`${CONSTANTS.routes.home}portfolios/${portfolio.portfolioId}`}
              className="shrink-0 text-[#2660a6] hover:underline"
              onClick={(event) => event.stopPropagation()}
            >
              {portfolio.portfolioName}
            </Link>
          </h3>
          <p className="mt-1 text-xs text-gray-500">
            {breaches.length}{" "}
            {breaches.length === 1
              ? STRINGS.breaches.breachSingular
              : STRINGS.breaches.breachPlural}
          </p>
        </div>
        <div className="shrink-0">
          {isExpanded ? (
            <ChevronUp className="h-5 w-5 text-[#2660a6]" aria-hidden="true" />
          ) : (
            <ChevronDown className="h-5 w-5 text-gray-400" aria-hidden="true" />
          )}
        </div>
      </button>

      {/* Breaches list (expanded) */}
      {isExpanded && (
        <div className="border-t border-[#e5e7eb] pt-2 pb-4 px-4 bg-gray-50 space-y-3">
          {breaches.map((breach) => (
            <BreachItem
              key={breach.breachId}
              breach={breach}
              onAccept={onBreachAccepted}
              isLoading={loadingBreachIds.has(breach.breachId)}
              isAcknowledged={breach.status === "ACKNOWLEDGED"}
            />
          ))}
        </div>
      )}
    </div>
  );
}
