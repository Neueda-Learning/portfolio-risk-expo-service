"use client";

import { useMemo, useState } from "react";
import type { PortfolioDetails, PortfolioPositionWithInstrument } from "@/types";
import { PortfolioStats } from "./PortfolioStats";
import { PositionTable } from "./PositionTable";
import { PositionDetails } from "./PositionDetails";

export function PortfolioDetailsClient({
  details,
}: {
  details: PortfolioDetails;
}) {
  const [search, setSearch] = useState("");
  const [selectedPositionId, setSelectedPositionId] = useState(
    details.positions[0]?.positionId ?? null
  );
  const [sortColumn, setSortColumn] = useState<string>("instrument");
  const [sortDirection, setSortDirection] = useState<"asc" | "desc">("asc");

  const filteredAndSortedPositions = useMemo(() => {
    const term = search.trim().toLowerCase();
    let positions = details.positions;

    if (term) {
      positions = positions.filter((position) =>
        position.instrument.instrumentName.toLowerCase().includes(term)
      );
    }

    return [...positions].sort((a, b) => {
      let aVal: string | number = "";
      let bVal: string | number = "";

      switch (sortColumn) {
        case "instrument":
          aVal = a.instrument.instrumentName.toLowerCase();
          bVal = b.instrument.instrumentName.toLowerCase();
          break;
        case "isin":
          aVal = (a.instrument.instrumentIsin ?? "").toLowerCase();
          bVal = (b.instrument.instrumentIsin ?? "").toLowerCase();
          break;
        case "assetClass":
          aVal = (a.instrument.assetClass ?? "").toLowerCase();
          bVal = (b.instrument.assetClass ?? "").toLowerCase();
          break;
        case "weight":
          aVal = a.weightPct;
          bVal = b.weightPct;
          break;
        case "marketValue":
          aVal = a.marketValueBase;
          bVal = b.marketValueBase;
          break;
        default:
          return 0;
      }

      if (typeof aVal === "string" && typeof bVal === "string") {
        return sortDirection === "asc" ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);
      }

      const numA = Number(aVal);
      const numB = Number(bVal);
      return sortDirection === "asc" ? numA - numB : numB - numA;
    });
  }, [details.positions, search, sortColumn, sortDirection]);

  const handleSortClick = (column: string) => {
    if (sortColumn === column) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortColumn(column);
      setSortDirection("asc");
    }
  };

  const filteredPositions = filteredAndSortedPositions;

  const selectedPosition: PortfolioPositionWithInstrument | null = useMemo(() => {
    return (
      details.positions.find((position) => position.positionId === selectedPositionId) ??
      filteredPositions[0] ??
      null
    );
  }, [details.positions, filteredPositions, selectedPositionId]);

  return (
    <div className="space-y-6">
      <PortfolioStats details={details} />

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.6fr)_minmax(320px,0.9fr)]">
        <PositionTable
          positions={filteredPositions}
          search={search}
          onSearchChange={setSearch}
          sortColumn={sortColumn}
          sortDirection={sortDirection}
          onSortClick={handleSortClick}
          selectedPositionId={selectedPositionId}
          onPositionSelect={setSelectedPositionId}
          baseCurrency={details.baseCurrency}
          totalPositions={details.positions.length}
        />

        <PositionDetails
          selectedPosition={selectedPosition}
          baseCurrency={details.baseCurrency}
        />
      </div>
    </div>
  );
}
