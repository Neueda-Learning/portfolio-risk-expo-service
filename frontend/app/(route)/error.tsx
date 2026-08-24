"use client";

import { useEffect } from "react";
import { AlertTriangle, RefreshCw, ServerOff } from "lucide-react";
import { STRINGS } from "@/lib/strings";

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("[Portfolio Risk] Page error:", error);
  }, [error]);

  const isConnectionError = error.message.includes("reach the backend");

  return (
    <div className="flex min-h-[60vh] items-center justify-center px-4">
      <div className="w-full max-w-md rounded-lg border-2 border-[#2660a6] bg-white p-8 shadow-md text-center">
        <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-[#db0011]/10">
          {isConnectionError ? (
            <ServerOff className="h-7 w-7 text-[#db0011]" aria-hidden="true" />
          ) : (
            <AlertTriangle className="h-7 w-7 text-[#db0011]" aria-hidden="true" />
          )}
        </div>

        <h1 className="mb-1 text-lg font-bold text-gray-900">
          {isConnectionError
            ? STRINGS.errors.backendUnreachableTitle
            : STRINGS.errors.dataUnavailableTitle}
        </h1>

        <p className="mb-1 text-sm text-gray-600">
          {isConnectionError
            ? STRINGS.errors.backendUnreachableMessage
            : STRINGS.errors.dataUnavailableMessage}
        </p>

        {error.message && (
          <p className="mb-6 rounded-md bg-gray-50 px-3 py-2 font-mono text-xs text-gray-500 break-all">
            {error.message}
          </p>
        )}

        <button
          onClick={reset}
          className="inline-flex items-center gap-2 rounded-md bg-[#2660a6] px-5 py-2 text-sm font-semibold text-white hover:bg-[#1e4f8a] focus:outline-none focus:ring-2 focus:ring-[#2660a6] focus:ring-offset-2"
        >
          <RefreshCw className="h-4 w-4" aria-hidden="true" />
          {STRINGS.errors.tryAgain}
        </button>
      </div>
    </div>
  );
}
