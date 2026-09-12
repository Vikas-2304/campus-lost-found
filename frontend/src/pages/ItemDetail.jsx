import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";
import ItemForm from "../components/ItemForm";
import ClaimsPanel from "../components/ClaimsPanel";

export default function ItemDetail() {
    const { id } = useParams();
    const { loggedIn } = useAuth();
    const navigate = useNavigate();
    const [item, setItem] = useState(null);
    const [matches, setMatches] = useState([]);
    const [editing, setEditing] = useState(false);
    const [answer, setAnswer] = useState("");
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");

    useEffect(() => { load(); }, [id]);

    async function load() {
        try {
            const data = await api(`/api/items/${id}`);
            setItem(data);
            if (data.isOwner) {
                setMatches(await api(`/api/items/${id}/matches`));
            }
        } catch (err) {
            setError(err.message);
        }
    }

    async function submitClaim(e) {
        e.preventDefault();
        setError(""); setNotice("");
        try {
            await api(`/api/items/${id}/claims`, { method: "POST", body: { verificationAnswer: answer } });
            setNotice("Claim submitted! The owner will review it.");
            setAnswer("");
        } catch (err) {
            setError(err.message);
        }
    }

    async function updateItem(values) {
        setError("");
        try {
            const updated = await api(`/api/items/${id}`, { method: "PUT", body: values });
            setItem(updated);
            setEditing(false);
            setNotice("Report updated.");
        } catch (err) {
            setError(err.message);
        }
    }

    async function closeItem() {
        if (!window.confirm("Close this report? It will no longer accept claims.")) return;
        try {
            await api(`/api/items/${id}`, { method: "DELETE" });
            navigate("/mine");
        } catch (err) {
            setError(err.message);
        }
    }

    if (error && !item) return <p className="error">{error}</p>;
    if (!item) return <p>Loading…</p>;

    // Claim button rules: logged in, not the owner, item is FOUND and still active
    const canClaim = loggedIn && !item.isOwner && item.type === "FOUND" &&
        (item.status === "OPEN" || item.status === "MATCHED");

    return (
        <div>
            {editing ? (
                <div className="card form-card wide">
                    <h2>Edit Report</h2>
                    <ItemForm initial={item} submitLabel="Save changes" onSubmit={updateItem} />
                    <button className="link-btn" onClick={() => setEditing(false)}>Cancel</button>
                </div>
            ) : (
                <div className="card">
                    <div className="detail-header">
                        <span className={`badge ${item.type.toLowerCase()}`}>{item.type}</span>
                        <span className={`status-${item.status.toLowerCase()}`}>{item.status}</span>
                    </div>
                    <h1>{item.title}</h1>
                    <p>{item.description}</p>
                    <p className="muted">Category: {item.category} • Location: {item.location}</p>
                    <p className="muted">Event date: {item.eventDate} • Posted by: {item.postedByName}</p>
                    {item.imageUrl && <img src={item.imageUrl} alt={item.title} className="detail-img" />}

                    {item.status === "MATCHED" && !item.isOwner && (
                        <p className="match-banner">Possible match found for this report!</p>
                    )}

                    {item.isOwner && (
                        <>
                            {item.verificationQuestion && (
                                <p className="muted">Your private verification question: <b>{item.verificationQuestion}</b></p>
                            )}
                            <div className="row-btns">
                                <button className="primary" onClick={() => setEditing(true)}>Edit</button>
                                <button className="danger" onClick={closeItem}>Close report</button>
                            </div>
                            {matches.length > 0 && (
                                <div className="section">
                                    <h3>Possible matches</h3>
                                    {matches.map(m => (
                                        <Link key={m.matchId} to={`/items/${m.otherItemId}`} className="card match-card">
                                            <span className={`badge ${m.otherItemType.toLowerCase()}`}>{m.otherItemType}</span>
                                            <span>{m.otherItemTitle}</span>
                                            <span className="score">score {m.score}</span>
                                        </Link>
                                    ))}
                                </div>
                            )}
                            <ClaimsPanel itemId={id} />
                        </>
                    )}

                    {canClaim && (
                        <div className="section">
                            <h3>Claim this item</h3>
                            <form onSubmit={submitClaim} className="claim-form">
                                <label>Prove it's yours — the owner set a private verification question.
                                    Describe distinctive details (contents, scratches, serials…). The owner compares
                                    your answer against their question before approving.
                                    <textarea value={answer} onChange={e => setAnswer(e.target.value)} required />
                                </label>
                                <button className="primary" type="submit">Submit claim</button>
                            </form>
                        </div>
                    )}

                    {notice && <p className="notice">{notice}</p>}
                    {error && <p className="error">{error}</p>}
                </div>
            )}
        </div>
    );
}