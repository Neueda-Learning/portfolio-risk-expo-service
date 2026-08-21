export function MetricCell({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-white px-3 py-2">
      <p className="text-xs text-gray-400">{label}</p>
      <p className="tabular-nums text-sm font-semibold text-gray-800">{value}</p>
    </div>
  );
}
