import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function ItemDetail() {
    const { id } = useParams();
    const { loggedIn } = useAuth();

    const [item, setItem] = useState(null);
    const [claims, setClaims] = useState([]);
    const [matches, setMatches] = useState([]);
    const [answer, setAnswer] = useState("");
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");

    const load = useCallback(async () => {
        try {
            const data = await api(`/api/items/${id}`);
            setItem(data);
            if (data.ownedByCurrentUser) {
                const [c, m] = await Promise.all([
                    api(`/api/items/${id}/claims`),
                    api(`/api/items/${id}/matches`),
                ]);
                setClaims(c);
                setMatches(m);
            } else {
                setClaims([]);
                setMatches([]);
            }
        } catch (err) {
            setError(err.message);
        }
    }, [id]);

    useEffect(() => { load(); }, [load]);

    async function submitClaim(e) {
        e.preventDefault();
        setError(""); setNotice("");
        try {
            await api(`/api/items/${id}/claims`, {
                method: "POST",
                body: { verificationAnswer: answer },
            });
            setNotice("Claim submitted. The owner will review your answer.");
            setAnswer("");
        } catch (err) {
            setError(err.message);
        }
    }

    async function decide(claimId, action) {
        setError(""); setNotice("");
        try {
            await api(`/api/claims/${claimId}/${action}`, { method: "PUT" });
            await load();
        } catch (err) {
            setError(err.message);
        }
    }

    if (!item) return <p>Loading…</p>;

    const claimable = item.type === "FOUND" && (item.status === "OPEN" || item.status === "MATCHED");
    const canClaim = loggedIn && !item.ownedByCurrentUser && claimable;

    return (
        <div>
            <Link to="/" className="muted">← Back to browse</Link>

            <div className="card section">
                <div className="row-between">
                    <span className={`badge ${item.type.toLowerCase()}`}>{item.type}</span>
                    <span className="status">Status: {item.status}</span>
                </div>
                <h1>{item.title}</h1>
                <p className="muted">
                    {item.category} • {item.location} • event date {item.eventDate} • posted by {item.postedByName}
                </p>
                <p>{item.description}</p>
                {item.imageUrl && <img src={item.imageUrl} alt={item.title} className="detail-img" />}
                {item.ownedByCurrentUser && item.verificationQuestion && (
                    <p className="muted">Your private verification question: <b>{item.verificationQuestion}</b></p>
                )}
            </div>

            {error && <p className="error">{error}</p>}
            {notice && <p className="notice">{notice}</p>}

            {item.ownedByCurrentUser && matches.length > 0 && (
                <div className="card section match-banner">
                    <h2>Possible match found</h2>
                    {matches.map(m => (
                        <p key={m.matchId}>
                            <Link to={`/items/${m.otherItemId}`}>{m.otherItemTitle}</Link> (score {m.score})
                        </p>
                    ))}
                </div>
            )}

            {canClaim && (
                <div className="card section">
                    <h2>Is this yours?</h2>
                    <p className="muted">
                        The owner set a private verification question. Provide an identifying detail
                        only the true owner would know — the owner compares your answer against it.
                    </p>
                    <form onSubmit={submitClaim} className="claim-form">
                        <label>Your identifying detail
                            <input value={answer} onChange={e => setAnswer(e.target.value)} required />
                        </label>
                        <button type="submit" className="primary">Claim this item</button>
                    </form>
                </div>
            )}

            {!loggedIn && claimable && (
                <p className="notice"><Link to="/login">Log in</Link> to claim this item.</p>
            )}

            {item.ownedByCurrentUser && (
                <div className="card section">
                    <h2>Claims ({claims.length})</h2>
                    {claims.length === 0 && <p className="muted">No claims yet.</p>}
                    {claims.map(c => (
                        <div key={c.id} className="claim-row">
                            <div>
                                <b>{c.claimantName}</b> answered: “{c.verificationAnswer}”
                                <span className={`claim-status ${c.status.toLowerCase()}`}> — {c.status}</span>
                            </div>
                            {c.status === "PENDING" && (
                                <div className="claim-actions">
                                    <button className="approve" onClick={() => decide(c.id, "approve")}>Approve</button>
                                    <button className="reject" onClick={() => decide(c.id, "reject")}>Reject</button>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}