import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/client";

const emptyForm = {
    type: "LOST",
    title: "",
    description: "",
    category: "",
    location: "",
    imageUrl: "",
    eventDate: "",
    verificationQuestion: "",
};

export default function PostItem() {
    const { id } = useParams();           // present only in edit mode
    const editing = Boolean(id);
    const navigate = useNavigate();
    const [form, setForm] = useState(emptyForm);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!editing) return;
        api(`/api/items/${id}`)
            .then(item => {
                if (!item.ownedByCurrentUser) {
                    setError("You can only edit your own items.");
                    return;
                }
                setForm({
                    type: item.type,
                    title: item.title,
                    description: item.description,
                    category: item.category,
                    location: item.location,
                    imageUrl: item.imageUrl || "",
                    eventDate: item.eventDate,
                    verificationQuestion: item.verificationQuestion || "",
                });
            })
            .catch(err => setError(err.message));
    }, [id, editing]);

    function update(key, value) {
        setForm(f => ({ ...f, [key]: value }));
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        const body = { ...form };
        // LOST reports never carry a verification question (claims only happen on FOUND items)
        if (body.type === "LOST") body.verificationQuestion = null;
        try {
            const saved = editing
                ? await api(`/api/items/${id}`, { method: "PUT", body })
                : await api("/api/items", { method: "POST", body });
            navigate(`/items/${saved.id}`);
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div className="card form-card wide">
            <h1>{editing ? "Edit report" : "Post an item"}</h1>
            <form onSubmit={handleSubmit}>
                <div className="tabs">
                    <button type="button" className={form.type === "LOST" ? "tab active" : "tab"}
                            onClick={() => update("type", "LOST")}>I LOST something</button>
                    <button type="button" className={form.type === "FOUND" ? "tab active" : "tab"}
                            onClick={() => update("type", "FOUND")}>I FOUND something</button>
                </div>
                <label>Title
                    <input value={form.title} onChange={e => update("title", e.target.value)} required />
                </label>
                <label>Description
                    <textarea rows={4} value={form.description} onChange={e => update("description", e.target.value)} required />
                </label>
                <label>Category
                    <input placeholder="e.g. Electronics, Books, Accessories" value={form.category}
                           onChange={e => update("category", e.target.value)} required />
                </label>
                <label>Location
                    <input placeholder="e.g. Library, Cafeteria" value={form.location}
                           onChange={e => update("location", e.target.value)} required />
                </label>
                <label>Image URL (optional)
                    <input value={form.imageUrl} onChange={e => update("imageUrl", e.target.value)} />
                </label>
                <label>{form.type === "LOST" ? "Date lost" : "Date found"}
                    <input type="date" value={form.eventDate} onChange={e => update("eventDate", e.target.value)} required />
                </label>
                {form.type === "FOUND" && (
                    <label>Private verification question (only YOU see this; claimants answer blind)
                        <input placeholder="e.g. What color is the sticker on it?"
                               value={form.verificationQuestion}
                               onChange={e => update("verificationQuestion", e.target.value)} />
                    </label>
                )}
                {error && <p className="error">{error}</p>}
                <button type="submit" className="primary">{editing ? "Save changes" : "Post"}</button>
            </form>
        </div>
    );
}