import Link from "next/link";
import { AlertCircle, ChevronRight } from "lucide-react";
import { BreachRow } from "@/components/dashboard/BreachRow";
import { CONSTANTS } from "@/lib/constants";
import { STRINGS } from "@/lib/strings";
import type { LimitBreach } from "@/types";

export function GlobalAlertsPanel({ breaches }: { breaches: LimitBreach[] }) {
  return (
    <aside className="w-full shrink-0 lg:w-72 xl:w-80">
      <div className="rounded-lg border-2 border-[#2660a6] bg-white shadow-md">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[#e5e7eb] px-4 py-3">
          <h2 className="flex items-center gap-1.5 text-sm font-bold text-gray-900">
            <AlertCircle className="h-4 w-4 text-[#db0011]" aria-hidden="true" />
            {STRINGS.alerts.title}
          </h2>
          {breaches.length > 0 && (
            <span className="rounded-full bg-[#db0011] px-2 py-0.5 text-xs font-bold text-white">
              {breaches.length}
            </span>
          )}
        </div>

        {/* Breach list */}
        <div className="flex flex-col gap-2 p-3">
          {breaches.length === 0 ? (
            <p className="py-4 text-center text-sm text-gray-400">
              {STRINGS.alerts.none}
            </p>
          ) : (
            breaches.map((breach) => (
              <BreachRow key={breach.breachId} breach={breach} />
            ))
          )}
        </div>

        {/* Footer */}
        <div className="border-t border-[#e5e7eb] px-4 py-3">
          <Link
            href={CONSTANTS.routes.breaches}
            className="flex w-full items-center justify-center gap-1 rounded-md bg-[#2660a6] px-3 py-1.5 text-xs font-semibold text-white hover:bg-[#1e4f8a]"
          >
            {STRINGS.alerts.viewAll}
            <ChevronRight className="h-3.5 w-3.5" />
          </Link>
        </div>
      </div>
    </aside>
  );
}
