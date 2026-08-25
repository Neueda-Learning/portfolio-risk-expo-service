import { STRINGS } from "@/lib/strings";
import { CONSTANTS } from "@/lib/constants";

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const isServer = typeof window === "undefined";
  // Server-side: use Docker service name (backend), Client-side: use relative URL that Next.js rewrites
  const baseUrl = isServer ? CONSTANTS.api.backendUrl : "";
  const url = `${baseUrl}${path}`;
  const requestInit = isServer
    ? { ...init, next: { revalidate: CONSTANTS.api.revalidateSeconds } }
    : init;
  let res: Response;
  try {
    res = await fetch(url, requestInit);
  } catch {
    throw new Error(
      `${STRINGS.api.backendUnreachablePrefix} ${CONSTANTS.api.baseUrl}. ${STRINGS.api.backendRunningQuestion}`
    );
  }
  if (!res.ok) {
    throw new Error(
      `${STRINGS.api.backendReturnedPrefix} ${res.status} for ${path}.`
    );
  }
  return await res.json() as Promise<T>;
}
