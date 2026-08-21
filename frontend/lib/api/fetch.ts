import { STRINGS } from "@/lib/strings";
import { CONSTANTS } from "@/lib/constants";

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const isServer = typeof window === "undefined";
  const url = isServer ? `${CONSTANTS.api.baseUrl}${path}` : path;
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
  return res.json() as Promise<T>;
}
