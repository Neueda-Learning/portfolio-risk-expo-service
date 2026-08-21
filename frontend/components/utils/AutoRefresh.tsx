"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export function AutoRefresh({ everyMs = 20000 }: { everyMs?: number }) {
  const router = useRouter();

  useEffect(() => {
    const id = window.setInterval(() => {
      router.refresh();
    }, everyMs);

    return () => window.clearInterval(id);
  }, [router, everyMs]);

  return null;
}