import { CONSTANTS } from "@/lib/constants";
import { STRINGS } from "@/lib/strings";

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${CONSTANTS.api.baseUrl}${path}`, {
      ...init,
      next: { revalidate: CONSTANTS.api.revalidateSeconds },
    });
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
