"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { BreachesTabs } from "./BreachesTabs";
import { BreachPortfolioGroup } from "./BreachPortfolioGroup";
import { acknowledgeBreach } from "@/lib/api/breaches";
import { STRINGS } from "@/lib/strings";
import type { LimitBreach, Portfolio } from "@/types";

const ACCEPTED_BREACH_IDS_STORAGE_KEY = "breach-accepted-ids";

interface BreachesPageProps {
  allBreaches: LimitBreach[];
  portfolios: Portfolio[];
}

export function BreachesPage({ allBreaches, portfolios }: BreachesPageProps) {
  const [activeTab, setActiveTab] = useState<"breaches" | "accepted">("breaches");
  const [acceptedBreachIds, setAcceptedBreachIds] = useState<Set<number>>(new Set());
  const [loadingBreachIds, setLoadingBreachIds] = useState<Set<number>>(new Set());
  const [error, setError] = useState<string | null>(null);
  const [hasHydratedAcceptedIds, setHasHydratedAcceptedIds] = useState(false);

  useEffect(() => {
   try {
     const stored = localStorage.getItem(ACCEPTED_BREACH_IDS_STORAGE_KEY);
     console.log("[Hydration] localStorage read:", { key: ACCEPTED_BREACH_IDS_STORAGE_KEY, stored });
     if (stored) {
       const parsed = JSON.parse(stored);
       if (Array.isArray(parsed)) {
         setAcceptedBreachIds(
           new Set(parsed.filter((value): value is number => typeof value === "number"))
         );
         console.log("[Hydration] acceptedBreachIds set from localStorage:", parsed);
       }
     } else {
       console.log("[Hydration] localStorage key not found or empty");
     }
   } catch (err) {
     console.error("Failed to read accepted breaches from localStorage", err);
   } finally {
     setHasHydratedAcceptedIds(true);
     console.log("[Hydration] hydration complete");
   }
  }, []);

  useEffect(() => {
   if (!hasHydratedAcceptedIds) {
     return;
   }

   // Don't persist empty set on initial hydration
   if (acceptedBreachIds.size === 0) {
     return;
   }

   try {
     const data = Array.from(acceptedBreachIds);
     console.log("[Persist] Writing to localStorage:", { key: ACCEPTED_BREACH_IDS_STORAGE_KEY, data });
     localStorage.setItem(
       ACCEPTED_BREACH_IDS_STORAGE_KEY,
       JSON.stringify(data)
     );
     console.log("[Persist] Write complete");
   } catch (err) {
     console.error("[Persist] Failed to write to localStorage:", err);
   }
  }, [acceptedBreachIds, hasHydratedAcceptedIds]);

  const isBreachAccepted = useCallback(
   (breach: LimitBreach) =>
     breach.status === "ACKNOWLEDGED" || acceptedBreachIds.has(breach.breachId),
   [acceptedBreachIds]
  );

  const breachesForActiveTabByPortfolio = useMemo(() => {
    const grouped = new Map<number, LimitBreach[]>();

    allBreaches.forEach((breach) => {
      const accepted = isBreachAccepted(breach);
      
      // Breaches tab: show ONLY NOT accepted
      // Accepted tab: show ONLY accepted
      if (activeTab === "breaches" && accepted) {
        return;
      }
      if (activeTab === "accepted" && !accepted) {
        return;
      }

      if (!grouped.has(breach.portfolioId)) {
        grouped.set(breach.portfolioId, []);
      }

      grouped.get(breach.portfolioId)!.push(breach);
    });

    return grouped;
  }, [allBreaches, activeTab, isBreachAccepted]);

  const portfoliosWithBreaches = useMemo(() => {
   return portfolios.filter((portfolio) => breachesForActiveTabByPortfolio.has(portfolio.portfolioId));
  }, [portfolios, breachesForActiveTabByPortfolio]);

  const handleBreachAccepted = useCallback(
   async (breachId: number) => {
     setLoadingBreachIds((prev) => new Set(prev).add(breachId));
     setError(null);

     try {
       await acknowledgeBreach(breachId, {
         acknowledgedBy: "frontend",
         resolution: "Accepted via Breach Management",
       });

       setAcceptedBreachIds((prev) => {
         const next = new Set(prev);
         next.add(breachId);
         return next;
       });
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

  const breachesCount = allBreaches.filter((breach) => !isBreachAccepted(breach)).length;
  const acceptedCount = allBreaches.filter((breach) => isBreachAccepted(breach)).length;

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
                  acceptedBreachIds={acceptedBreachIds}
                />
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
