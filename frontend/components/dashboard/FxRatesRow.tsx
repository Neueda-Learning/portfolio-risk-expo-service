import { BarChart3 } from "lucide-react";
import { formatDecimal } from "@/lib/format";
import type { ExchangeRate } from "@/types";

export function FxRatesRow({
	exchangeRates,
	baseCurrency = "USD",
}: {
	exchangeRates: ExchangeRate[];
	baseCurrency?: string;
}) {
	const rates = [...exchangeRates].sort((a, b) =>
		a.toCurrency.localeCompare(b.toCurrency)
	);

	return (
		<section className="mb-6 rounded-lg border-2 border-[#2660a6] bg-white shadow-md">
			<div className="flex items-center justify-between gap-3 border-b border-[#e5e7eb] px-4 py-3">
				<h2 className="flex items-center gap-1.5 text-sm font-bold text-gray-900">
					<BarChart3 className="h-4 w-4 text-[#2660a6]" aria-hidden="true" />
					FX Rates
				</h2>
				<span className="rounded-full bg-[#2660a6]/10 px-2 py-0.5 text-xs font-semibold text-[#2660a6]">
					Base {baseCurrency}
				</span>
			</div>

			<div className="overflow-x-auto px-3 py-3">
				{rates.length === 0 ? (
					<p className="py-4 text-center text-sm text-gray-400">
						No FX rates available.
					</p>
				) : (
					<div className="flex min-w-max gap-2">
						{rates.map((rate) => (
							<div
								key={`${rate.fromCurrency}-${rate.toCurrency}`}
								className="flex min-w-36 flex-1 items-center justify-between gap-3 rounded-md border border-[#e5e7eb] bg-[#f9fafb] px-3 py-2 shadow-sm"
							>
								<div className="min-w-0">
									<p className="text-[11px] font-medium uppercase tracking-wide text-gray-400">
										{rate.fromCurrency} to {rate.toCurrency}
									</p>
									<p className="truncate text-sm font-semibold text-gray-900">
										1 {rate.fromCurrency}
									</p>
								</div>
								<div className="shrink-0 text-right">
									<p className="text-sm font-bold text-[#2660a6]">
										{formatDecimal(rate.rate, 4)}
									</p>
									<p className="text-[11px] text-gray-500">
										{rate.toCurrency}
									</p>
								</div>
							</div>
						))}
					</div>
				)}
			</div>
		</section>
	);
}
