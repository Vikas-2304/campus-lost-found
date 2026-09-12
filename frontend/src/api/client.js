const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

// One fetch wrapper for the whole app: attaches the JWT automatically
// and turns error responses into clean Error messages.
export async function api(path, { method = "GET", body } = {}) {
    const headers = { "Content-Type": "application/json" };
    const token = localStorage.getItem("token");
    if (token) headers.Authorization = `Bearer ${token}`;

    const res = await fetch(`${API_URL}${path}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });

    if (res.status === 204) return null;
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
        throw new Error(data.error || JSON.stringify(data) || res.statusText);
    }
    return data;
}