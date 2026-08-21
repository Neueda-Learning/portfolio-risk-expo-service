"use client";

import { useCallback, useMemo, useState } from "react";
import { BreachesTabs } from "./BreachesTabs";
import { BreachPortfolioGroup } from "./BreachPortfolioGroup";
import { acknowledgeBreach } from "@/lib/api/breaches";
import { STRINGS } from "@/lib/strings";
import type { LimitBreach, Portfolio } from "@/types";

interface BreachesPageProps {
  allBreaches: LimitBreach[];
  portfolios: Portfolio[];
}

export function BreachesPage({ allBreaches, portfolios }: BreachesPageProps) {
  const [activeTab, setActiveTab] = useState<"breaches" | "accepted">("breaches");
  const [acceptedBreachIds, setAcceptedBreachIds] = useState<Set<number>>(new Set());
  const [loadingBreachIds, setLoadingBreachIds] = useState<Set<number>>(new Set());
  const [error, setError] = useState<string | null>(null);

  // Filter breaches based on status and tab
  const filteredBreaches = useMemo(() => {
    const openBreaches = allBreaches.filter(
      (b) => b.status === "OPEN" && !acceptedBreachIds.has(b.breachId)
    );
    const acknowledgedBreaches = allBreaches.filter(
      (b) => b.status === "ACKNOWLEDGED" || acceptedBreachIds.has(b.breachId)
    );

    return {
      open: openBreaches,
      acknowledged: acknowledgedBreaches,
    };
  }, [allBreaches, acceptedBreachIds]);

  // Group breaches by portfolio
  const breachesByPortfolio = useMemo(() => {
    const grouped = new Map<number, LimitBreach[]>();

    const breaches =
      activeTab === "breaches" ? filteredBreaches.open : filteredBreaches.acknowledged;

    breaches.forEach((breach) => {
      if (!grouped.has(breach.portfolioId)) {
        grouped.set(breach.portfolioId, []);
      }
      grouped.get(breach.portfolioId)!.push(breach);
    });

    return grouped;
  }, [filteredBreaches, activeTab]);

  // Get portfolios that have breaches in current tab
  const portfoliosWithBreaches = useMemo(() => {
    return portfolios.filter((p) => breachesByPortfolio.has(p.portfolioId));
  }, [portfolios, breachesByPortfolio]);

  const handleBreachAccepted = useCallback(
    async (breachId: number) => {
      setLoadingBreachIds((prev) => new Set(prev).add(breachId));
      setError(null);

      try {
        await acknowledgeBreach(breachId);
        setAcceptedBreachIds((prev) => new Set(prev).add(breachId));
      } catch (err) {
        setError(STRINGS.breaches.failedToAccept);
      } finally {
        setLoadingBreachIds((prev) => {
          const next = new Set(prev);
          next.delete(breachId);
          return next;
        });
      }
    },
    []
  );

  const breachesCount = filteredBreaches.open.length;
  const acceptedCount = filteredBreaches.acknowledged.length;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-[#e5e7eb] px-6 py-4">
        <h1 className="text-2xl font-bold text-gray-900">{STRINGS.breaches.title}</h1>
        <p className="mt-1 text-sm text-gray-500">
          {activeTab === "breaches"
            ? `${breachesCount} ${
                breachesCount === 1
                  ? STRINGS.breaches.openBreachSingular
                  : STRINGS.breaches.openBreachPlural
              }`
            : `${acceptedCount} ${
                acceptedCount === 1
                  ? STRINGS.breaches.acceptedBreachSingular
                  : STRINGS.breaches.acceptedBreachPlural
              }`}
        </p>
      </div>

      {/* Tabs */}
      <BreachesTabs
        activeTab={activeTab}
        onTabChange={setActiveTab}
        breachesCount={breachesCount}
        acceptedCount={acceptedCount}
      />

      {/* Error message */}
      {error && (
        <div className="mx-4 mt-4 rounded-md bg-red-50 p-3 text-sm text-[#db0011] border border-[#db0011]/20">
          {error}
        </div>
      )}

      {/* Content */}
      <div className="p-6">
        {portfoliosWithBreaches.length === 0 ? (
          <div className="rounded-lg border border-dashed border-gray-200 bg-white px-4 py-12 text-center">
            <p className="text-sm text-gray-500">
              {activeTab === "breaches"
                ? STRINGS.breaches.noOpenBreaches
                : STRINGS.breaches.noAcceptedBreaches}
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {portfoliosWithBreaches.map((portfolio) => {
              const breaches = breachesByPortfolio.get(portfolio.portfolioId) || [];
              return (
                <BreachPortfolioGroup
                  key={portfolio.portfolioId}
                  portfolio={portfolio}
                  breaches={breaches}
                  onBreachAccepted={handleBreachAccepted}
                  acceptedBreachIds={loadingBreachIds}
                />
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
