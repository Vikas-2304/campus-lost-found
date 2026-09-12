import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";

export default function MyReports() {
    const [items, setItems] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => { load(); }, []);

    async function load() {
        try {
            setItems(await api("/api/items/mine"));
        } catch (err) {
            setError(err.message);
        }
    }

    async function closeItem(id) {
        if (!window.confirm("Close this report? It will no longer accept claims.")) return;
        try {
            await api(`/api/items/${id}`, { method: "DELETE" });
            load();
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div>
            <h1>My Reports</h1>
            {error && <p className="error">{error}</p>}
            {items.length === 0 && <p>You haven't posted anything yet.</p>}
            <div className="list">
                {items.map(item => (
                    <div key={item.id} className="card row-card">
                        <div>
                            <span className={`badge ${item.type.toLowerCase()}`}>{item.type}</span>
                            <Link to={`/items/${item.id}`}><b> {item.title}</b></Link>
                            <p className="muted">{item.category} • {item.location} • {item.eventDate}</p>
                        </div>
                        <div className="row-btns">
                            <span className={`status-${item.status.toLowerCase()}`}>{item.status}</span>
                            {item.status !== "CLOSED" && item.status !== "CLAIMED" && (
                                <button className="danger" onClick={() => closeItem(item.id)}>Close</button>
                            )}
                            <Link className="primary btn-link" to={`/items/${item.id}`}>Review claims & matches</Link>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}