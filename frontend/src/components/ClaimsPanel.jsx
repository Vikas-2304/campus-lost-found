import { useEffect, useState } from "react";
import { api } from "../api/client";

export default function ClaimsPanel({ itemId }) {
    const [claims, setClaims] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => { load(); }, [itemId]);

    async function load() {
        try {
            setClaims(await api(`/api/items/${itemId}/claims`));
        } catch (err) {
            setError(err.message);
        }
    }

    async function decide(claimId, action) {
        setError("");
        try {
            await api(`/api/claims/${claimId}/${action}`, { method: "PUT" });
            load();
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div className="section">
            <h3>Claims</h3>
            {error && <p className="error">{error}</p>}
            {claims.length === 0 && <p className="muted">No claims yet.</p>}
            {claims.map(c => (
                <div key={c.id} className="card claim-card">
                    <p><b>{c.claimantName}</b> <span className={`status-${c.status.toLowerCase()}`}>[{c.status}]</span></p>
                    <p className="muted">Their answer: "{c.verificationAnswer}"</p>
                    <p className="muted">{new Date(c.createdAt).toLocaleString()}</p>
                    {c.status === "PENDING" && (
                        <div className="row-btns">
                            <button className="approve" onClick={() => decide(c.id, "approve")}>Approve</button>
                            <button className="danger" onClick={() => decide(c.id, "reject")}>Reject</button>
                        </div>
                    )}
                </div>
            ))}
        </div>
    );
}