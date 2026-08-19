import type { Metadata } from "next";
import Link from "next/link";
import { Bell, BarChart2, Search } from "lucide-react";
import "../globals.css";

export const metadata: Metadata = {
  title: "Portfolio Risk & Exposure",
  description: "Portfolio Risk & Exposure Service — Risk Busters",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-[#f4f4f2]">
        {/* ── Top Navigation Bar ─────────────────────────────────────── */}
        <header className="sticky top-0 z-50 bg-[#2660a6] shadow-md">
          <div className="mx-auto flex h-14 max-w-screen-2xl items-center gap-4 px-4 sm:px-6">
            {/* Logo / Brand */}
            <Link
              href="/"
              className="flex shrink-0 items-center gap-2 text-white"
            >
              <BarChart2 className="h-6 w-6" aria-hidden="true" />
              <span className="hidden text-sm font-semibold tracking-wide sm:block">
                Risk &amp; Exposure
              </span>
            </Link>

            {/* Search */}
            <div className="relative flex-1 max-w-md">
              <Search
                className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-white/60"
                aria-hidden="true"
              />
              <input
                type="search"
                placeholder="Search portfolios…"
                className="w-full rounded-md bg-white/15 py-1.5 pl-9 pr-3 text-sm text-white placeholder-white/60 outline-none ring-1 ring-white/20 focus:bg-white/20 focus:ring-white/50"
              />
            </div>

            <div className="ml-auto flex items-center gap-3">
              {/* Notification Bell */}
              <button
                type="button"
                aria-label="Notifications"
                className="relative rounded-full p-1.5 text-white/80 hover:bg-white/15 hover:text-white"
              >
                <Bell className="h-5 w-5" />
                {/* Static unread badge */}
                <span className="absolute right-1 top-1 h-2 w-2 rounded-full bg-[#db0011]" />
              </button>
            </div>
          </div>
        </header>

        {/* ── Page Content ───────────────────────────────────────────── */}
        <main className="mx-auto max-w-screen-2xl px-4 py-6 sm:px-6">
          {children}
        </main>
      </body>
    </html>
  );
}
