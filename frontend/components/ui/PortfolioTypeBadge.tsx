import { TrendingUp, Building2, Layers, Shield, Banknote } from "lucide-react";
import type { PortfolioType } from "@/types";

const TYPE_CONFIG: Record<
  PortfolioType,
  { label: string; icon: React.ElementType; colour: string }
> = {
  EQUITY:       { label: "Equity",       icon: TrendingUp, colour: "bg-blue-50   text-blue-700   border-blue-200"   },
  FIXED_INCOME: { label: "Fixed Income", icon: Building2,  colour: "bg-green-50  text-green-700  border-green-200"  },
  MULTI_ASSET:  { label: "Multi-Asset",  icon: Layers,     colour: "bg-purple-50 text-purple-700 border-purple-200" },
  HEDGE:        { label: "Hedge",        icon: Shield,     colour: "bg-amber-50  text-amber-700  border-amber-200"  },
  MONEY_MKT:    { label: "Money Market", icon: Banknote,   colour: "bg-teal-50   text-teal-700   border-teal-200"   },
};

export function PortfolioTypeBadge({ type }: { type: PortfolioType }) {
  const { label, icon: Icon, colour } = TYPE_CONFIG[type];
  return (
    <span
      className={`flex shrink-0 items-center gap-1 rounded-full border px-2 py-0.5 text-xs font-medium ${colour}`}
    >
      <Icon className="h-3 w-3" aria-hidden="true" />
      {label}
    </span>
  );
}
