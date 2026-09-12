import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";

export default function Browse() {
    const [items, setItems] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [filters, setFilters] = useState({ type: "", category: "", location: "", from: "", to: "" });
    const [error, setError] = useState("");

    useEffect(() => { load(); }, [page, filters]);

    async function load() {
        setError("");
        const params = new URLSearchParams();
        if (filters.type) params.set("type", filters.type);
        if (filters.category) params.set("category", filters.category);
        if (filters.location) params.set("location", filters.location);
        if (filters.from) params.set("from", filters.from);
        if (filters.to) params.set("to", filters.to);
        params.set("page", page);
        params.set("size", 6);
        try {
            const data = await api(`/api/items?${params.toString()}`);
            setItems(data.content);
            setTotalPages(data.totalPages);
        } catch (err) {
            setError(err.message);
        }
    }

    function update(key, value) {
        setPage(0);
        setFilters(f => ({ ...f, [key]: value }));
    }

    return (
        <div>
            <h1>Browse Items</h1>
            <div className="filters card">
                <select value={filters.type} onChange={e => update("type", e.target.value)}>
                    <option value="">All types</option>
                    <option value="LOST">Lost</option>
                    <option value="FOUND">Found</option>
                </select>
                <input placeholder="Category" value={filters.category} onChange={e => update("category", e.target.value)} />
                <input placeholder="Location" value={filters.location} onChange={e => update("location", e.target.value)} />
                <label>From <input type="date" value={filters.from} onChange={e => update("from", e.target.value)} /></label>
                <label>To <input type="date" value={filters.to} onChange={e => update("to", e.target.value)} /></label>
            </div>
            {error && <p className="error">{error}</p>}
            <div className="item-grid">
                {items.map(item => (
                    <Link to={`/items/${item.id}`} key={item.id} className="card item-card">
                        <span className={`badge ${item.type.toLowerCase()}`}>{item.type}</span>
                        <h3>{item.title}</h3>
                        <p>{item.category} • {item.location}</p>
                        <p className="muted">{item.eventDate}</p>
                        <span className="status">{item.status}</span>
                    </Link>
                ))}
                {items.length === 0 && !error && <p>No items found. Try changing filters.</p>}
            </div>
            <div className="pagination">
                <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>Prev</button>
                <span>Page {page + 1} of {Math.max(totalPages, 1)}</span>
                <button disabled={page + 1 >= totalPages} onClick={() => setPage(p => p + 1)}>Next</button>
            </div>
        </div>
    );
}