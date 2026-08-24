"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { BreachesTabs } from "./BreachesTabs";
import { BreachPortfolioGroup } from "./BreachPortfolioGroup";
import { acknowledgeBreach, getBreaches } from "@/lib/api/breaches";
import { STRINGS } from "@/lib/strings";
import type { LimitBreach, Portfolio } from "@/types";

interface BreachesPageProps {
  initialAllBreaches: LimitBreach[];
  portfolios: Portfolio[];
}

export function BreachesPage({ initialAllBreaches, portfolios }: BreachesPageProps) {
  const [activeTab, setActiveTab] = useState<"breaches" | "accepted">("breaches");
  const [allBreaches, setAllBreaches] = useState<LimitBreach[]>(initialAllBreaches);
  const [loadingBreachIds, setLoadingBreachIds] = useState<Set<number>>(new Set());
  const [error, setError] = useState<string | null>(null);
  const [isFetching, setIsFetching] = useState(false);

  // Fetch both OPEN and ACKNOWLEDGED breaches on mount
  useEffect(() => {
   const fetchAllBreaches = async () => {
     setIsFetching(true);
     try {
       // Fetch OPEN and ACKNOWLEDGED breaches
       const [openBreaches, acknowledgedBreaches] = await Promise.all([
         getBreaches({ status: "OPEN" }),
         getBreaches({ status: "ACKNOWLEDGED" }),
       ]);
       // Combine both lists
       setAllBreaches([...openBreaches, ...acknowledgedBreaches]);
     } catch (err) {
       console.error("Failed to fetch breaches:", err);
       setError("Failed to load breaches");
     } finally {
       setIsFetching(false);
     }
   };

   fetchAllBreaches();
  }, []);

  const isBreachAccepted = useCallback(
   (breach: LimitBreach) => breach.status === "ACKNOWLEDGED",
   []
  );

  // Filter breaches by active tab
  const filteredBreaches = useMemo(() => {
   return allBreaches.filter((breach) => {
     if (activeTab === "breaches") {
       return breach.status === "OPEN";
     } else {
       return breach.status === "ACKNOWLEDGED";
     }
   });
  }, [allBreaches, activeTab]);

  const breachesForActiveTabByPortfolio = useMemo(() => {
    const grouped = new Map<number, LimitBreach[]>();

    filteredBreaches.forEach((breach) => {
      if (!grouped.has(breach.portfolioId)) {
        grouped.set(breach.portfolioId, []);
      }
      grouped.get(breach.portfolioId)!.push(breach);
    });

    return grouped;
  }, [filteredBreaches]);

  const portfoliosWithBreaches = useMemo(() => {
   return portfolios.filter((portfolio) => breachesForActiveTabByPortfolio.has(portfolio.portfolioId));
  }, [portfolios, breachesForActiveTabByPortfolio]);

  const handleBreachAccepted = useCallback(
   async (breachId: number) => {
     setLoadingBreachIds((prev) => new Set(prev).add(breachId));
     setError(null);

     try {
       const response = await acknowledgeBreach(breachId, {
         acknowledgedBy: "frontend",
         resolution: "Accepted via Breach Management",
       });

       // Update allBreaches with the acknowledged breach
       setAllBreaches((prev) =>
         prev.map((breach) =>
           breach.breachId === breachId
             ? { ...breach, status: "ACKNOWLEDGED" as const }
             : breach
         )
       );

       // Switch to Accepted tab to show the newly acknowledged breach
       setActiveTab("accepted");
     } catch (err) {
       console.error("Failed to acknowledge breach", err);
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

  const breachesCount = allBreaches.filter((breach) => breach.status === "OPEN").length;
  const acceptedCount = allBreaches.filter((breach) => breach.status === "ACKNOWLEDGED").length;

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

      {error && (
        <div className="mx-4 mt-4 rounded-md border border-[#db0011]/20 bg-red-50 p-3 text-sm text-[#db0011]">
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
              const breaches = breachesForActiveTabByPortfolio.get(portfolio.portfolioId) || [];
              return (
                <BreachPortfolioGroup
                  key={portfolio.portfolioId}
                  portfolio={portfolio}
                  breaches={breaches}
                  onBreachAccepted={handleBreachAccepted}
                  loadingBreachIds={loadingBreachIds}
                />
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
