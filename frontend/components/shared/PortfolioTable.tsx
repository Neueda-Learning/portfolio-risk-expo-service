"use client";

import type { ReactNode } from "react";
import { ChevronDown, ChevronUp, Search } from "lucide-react";

type SortDirection = "asc" | "desc";
type PortfolioTableTone = "blue" | "green" | "red";

const TONE_STYLES: Record<
  PortfolioTableTone,
  {
    border: string;
    title: string;
    focus: string;
  }
> = {
  blue: {
    border: "border-[#e5e7eb]",
    title: "text-gray-900",
    focus: "focus:border-[#2660a6] focus:ring-[#2660a6]",
  },
  green: {
    border: "border-[#16a34a]",
    title: "text-[#166534]",
    focus: "focus:border-[#16a34a] focus:ring-[#16a34a]",
  },
  red: {
    border: "border-[#db0011]",
    title: "text-[#7f1d1d]",
    focus: "focus:border-[#db0011] focus:ring-[#db0011]",
  },
};

export function PortfolioSortableHeader({
  label,
  isSorted,
  sortDirection,
  isNumeric = false,
  onClick,
}: {
  label: string;
  isSorted: boolean;
  sortDirection: SortDirection;
  isNumeric?: boolean;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={`flex items-center gap-2 font-semibold hover:text-gray-700 ${
        isNumeric ? "justify-end" : ""
      }`}
    >
      {label}
      {isSorted ? (
        sortDirection === "asc" ? (
          <ChevronUp className="h-4 w-4 text-[#2660a6]" />
        ) : (
          <ChevronDown className="h-4 w-4 text-[#2660a6]" />
        )
      ) : (
        <span className="h-4 w-4" />
      )}
    </button>
  );
}

interface PortfolioTableProps {
  title: string;
  summary?: ReactNode;
  searchValue?: string;
  searchPlaceholder?: string;
  onSearchChange?: (value: string) => void;
  actions?: ReactNode;
  tone?: PortfolioTableTone;
  children: ReactNode;
}

export function PortfolioTable({
  title,
  summary,
  searchValue,
  searchPlaceholder,
  onSearchChange,
  actions,
  tone = "blue",
  children,
}: PortfolioTableProps) {
  const showSearch = typeof onSearchChange === "function" && searchValue !== undefined;
  const styles = TONE_STYLES[tone];

  return (
    <section className={`min-w-0 rounded-lg border bg-white shadow-sm ${styles.border}`}>
      <div className={`flex flex-wrap items-center justify-between gap-3 border-b p-4 ${styles.border}`}>
        <div className="min-w-0">
          <h2 className={`text-lg font-semibold ${styles.title}`}>{title}</h2>
          {summary ? <div className="mt-1 text-sm text-gray-500">{summary}</div> : null}
        </div>

        <div className="flex w-full flex-wrap items-center justify-end gap-3 sm:w-auto">
          {actions}
          {showSearch ? (
            <div className="relative w-full max-w-sm">
              <Search
                className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400"
                aria-hidden="true"
              />
              <input
                type="search"
                value={searchValue}
                onChange={(event) => onSearchChange?.(event.target.value)}
                placeholder={searchPlaceholder}
                className={`w-full rounded-md border border-gray-200 bg-white py-2 pl-9 pr-3 text-sm text-gray-900 outline-none focus:ring-1 ${styles.focus}`}
              />
            </div>
          ) : null}
        </div>
      </div>

      {children}
    </section>
  );
}
