"use client";

import { STRINGS } from "@/lib/strings";

interface BreachesTabsProps {
  activeTab: "breaches" | "accepted";
  onTabChange: (tab: "breaches" | "accepted") => void;
  breachesCount: number;
  acceptedCount: number;
}

export function BreachesTabs({
  activeTab,
  onTabChange,
  breachesCount,
  acceptedCount,
}: BreachesTabsProps) {
  return (
    <div className="border-b border-[#e5e7eb] bg-white">
      <div className="flex gap-0">
        <button
          onClick={() => onTabChange("breaches")}
          className={`flex-1 px-6 py-3 text-sm font-semibold text-center transition-colors border-b-2 ${
            activeTab === "breaches"
              ? "text-[#2660a6] border-[#2660a6]"
              : "text-gray-600 border-transparent hover:text-gray-900"
          }`}
        >
          {STRINGS.breaches.tabBreaches}
          {breachesCount > 0 && (
            <span className="ml-2 inline-block rounded-full bg-[#db0011] px-2 py-0.5 text-xs font-bold text-white">
              {breachesCount}
            </span>
          )}
        </button>
        <button
          onClick={() => onTabChange("accepted")}
          className={`flex-1 px-6 py-3 text-sm font-semibold text-center transition-colors border-b-2 ${
            activeTab === "accepted"
              ? "text-[#2660a6] border-[#2660a6]"
              : "text-gray-600 border-transparent hover:text-gray-900"
          }`}
        >
          {STRINGS.breaches.tabAccepted}
          {acceptedCount > 0 && (
            <span className="ml-2 inline-block rounded-full bg-green-500 px-2 py-0.5 text-xs font-bold text-white">
              {acceptedCount}
            </span>
          )}
        </button>
      </div>
    </div>
  );
}
