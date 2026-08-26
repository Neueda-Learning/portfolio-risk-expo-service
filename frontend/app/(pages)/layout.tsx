import type { Metadata } from "next";
import { Inter } from "next/font/google";
import Link from "next/link";
import { BarChart2 } from "lucide-react";
import "../globals.css";
import { CONSTANTS } from "@/lib/constants";
import { STRINGS } from "@/lib/strings";
import { AutoRefresh } from "@/components/utils/AutoRefresh";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
});

export const metadata: Metadata = {
  title: STRINGS.app.title,
  description: STRINGS.app.description,
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      {/*<AutoRefresh everyMs={30000} />*/}
      <html lang="en" className={inter.variable}>
      <body className={`${inter.className} min-h-screen bg-[#f4f4f2]`}>
        {/* ── Top Navigation Bar ─────────────────────────────────────── */}
        <header className="sticky top-0 z-50 bg-[#2660a6] shadow-md">
          <div className="mx-auto flex h-14 max-w-screen-2xl items-center gap-4 px-4 sm:px-6">
            {/* Logo / Brand */}
            <Link
              href={CONSTANTS.routes.home}
              className="flex shrink-0 items-center gap-2 text-white"
            >
              <BarChart2 className="h-6 w-6" aria-hidden="true" />
              <span className="hidden text-sm font-semibold tracking-wide sm:block">
                {STRINGS.app.title}
              </span>
            </Link>
          </div>
        </header>

        {/* ── Page Content ───────────────────────────────────────────── */}
        <main className="mx-auto max-w-screen-2xl px-4 py-6 sm:px-6">
          {children}
        </main>
      </body>
    </html>
    </>
  );
}
