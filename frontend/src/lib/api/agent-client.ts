import { API } from './endpoints';

function getAuthHeaders(): Record<string, string> {
  if (typeof window === 'undefined') return {};
  const token = localStorage.getItem('accessToken');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function agentGet<T>(endpoint: string): Promise<T> {
  const res = await fetch(endpoint, { headers: getAuthHeaders() });
  if (!res.ok) throw new Error(`Agent request failed: ${res.status}`);
  return res.json();
}

export async function agentPost<T>(endpoint: string, body?: unknown): Promise<T> {
  const res = await fetch(endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`Agent request failed: ${res.status}`);
  return res.json();
}

export { API };
