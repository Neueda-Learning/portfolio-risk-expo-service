export function formatCurrency(
  value: number,
  currency: string,
  compact = true
): string {
  if (compact) {
    if (Math.abs(value) >= 1_000_000_000)
      return `${currency} ${(value / 1_000_000_000).toFixed(2)}B`;
    if (Math.abs(value) >= 1_000_000)
      return `${currency} ${(value / 1_000_000).toFixed(1)}M`;
    if (Math.abs(value) >= 1_000)
      return `${currency} ${(value / 1_000).toFixed(0)}K`;
  }
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency,
    maximumFractionDigits: 0,
  }).format(value);
}
