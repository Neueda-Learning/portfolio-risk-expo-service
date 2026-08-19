import type { BreachSeverity } from "@/types";

const SEVERITY_COLOUR: Record<BreachSeverity, string> = {
  CRITICAL: "bg-[#db0011]/10 text-[#db0011] border-[#db0011]/30",
  MAJOR:    "bg-[#d97706]/10 text-[#d97706] border-[#d97706]/30",
  MINOR:    "bg-yellow-50    text-yellow-700 border-yellow-200",
};

export function SeverityBadge({ severity }: { severity: BreachSeverity }) {
  return (
    <span
      className={`rounded-full border px-2 py-0.5 text-xs font-semibold ${SEVERITY_COLOUR[severity] ?? SEVERITY_COLOUR.MINOR}`}
    >
      {severity}
    </span>
  );
}
