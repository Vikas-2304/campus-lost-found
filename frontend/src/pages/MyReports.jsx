import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";

export default function MyReports() {
    const [items, setItems] = useState([]);
    const [error, setError] = useState("");

    const load = useCallback(async () => {
        try {
            setItems(await api("/api/items/mine"));
        } catch (err) {
            setError(err.message);
        }
    }, []);

    useEffect(() => { load(); }, [load]);

    async function closeItem(itemId) {
        if (!window.confirm("Close this report? It will stop appearing in browse and stop accepting claims.")) return;
        try {
            await api(`/api/items/${itemId}`, { method: "DELETE" });
            await load();
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div>
            <h1>My Reports</h1>
            {error && <p className="error">{error}</p>}
            {items.length === 0 && (
                <p>You haven't posted anything yet. <Link to="/post">Post your first item</Link>.</p>
            )}
            <div className="report-list">
                {items.map(item => (
                    <div key={item.id} className="card report-row">
                        <div>
                            <span className={`badge ${item.type.toLowerCase()}`}>{item.type}</span>
                            <b> {item.title}</b>
                            <span className={`item-status status-${item.status.toLowerCase()}`}> {item.status}</span>
                            <p className="muted">{item.category} • {item.location} • {item.eventDate}</p>
                        </div>
                        <div className="report-actions">
                            <Link to={`/items/${item.id}`}>View / review claims</Link>
                            <Link to={`/edit/${item.id}`}>Edit</Link>
                            {(item.status === "OPEN" || item.status === "MATCHED") && (
                                <button className="reject" onClick={() => closeItem(item.id)}>Close</button>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}