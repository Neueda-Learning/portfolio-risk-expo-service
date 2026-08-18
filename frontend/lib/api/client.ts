const API_BASE = "http://127.0.0.1:8080";

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      ...init,
      next: { revalidate: 30 },
    });
  } catch {
    throw new Error(
      `Could not reach the backend at ${API_BASE}. Is the server running?`
    );
  }
  if (!res.ok) {
    throw new Error(`Backend returned ${res.status} for ${path}.`);
  }
  return res.json() as Promise<T>;
}
