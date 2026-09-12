import { useState } from "react";

export default function ItemForm({ initial, submitLabel, onSubmit }) {
    const [form, setForm] = useState({
        type: initial?.type || "LOST",
        title: initial?.title || "",
        description: initial?.description || "",
        category: initial?.category || "",
        location: initial?.location || "",
        imageUrl: initial?.imageUrl || "",
        eventDate: initial?.eventDate || new Date().toISOString().slice(0, 10),
        verificationQuestion: initial?.verificationQuestion || "",
    });

    function set(key, value) {
        setForm(f => ({ ...f, [key]: value }));
    }

    function handleSubmit(e) {
        e.preventDefault();
        const payload = { ...form };
        // The private question only makes sense on FOUND items
        if (payload.type === "LOST" || !payload.verificationQuestion) {
            payload.verificationQuestion = null;
        }
        onSubmit(payload);
    }

    return (
        <form onSubmit={handleSubmit}>
            <div className="tabs">
                <button type="button" className={form.type === "LOST" ? "tab active" : "tab"} onClick={() => set("type", "LOST")}>LOST</button>
                <button type="button" className={form.type === "FOUND" ? "tab active" : "tab"} onClick={() => set("type", "FOUND")}>FOUND</button>
            </div>
            <label>Title<input value={form.title} onChange={e => set("title", e.target.value)} required /></label>
            <label>Description<textarea value={form.description} onChange={e => set("description", e.target.value)} required /></label>
            <label>Category<input value={form.category} onChange={e => set("category", e.target.value)} required /></label>
            <label>Location<input value={form.location} onChange={e => set("location", e.target.value)} required /></label>
            <label>Event date<input type="date" value={form.eventDate} onChange={e => set("eventDate", e.target.value)} required /></label>
            <label>Image URL (optional)<input value={form.imageUrl} onChange={e => set("imageUrl", e.target.value)} /></label>
            {form.type === "FOUND" && (
                <label>Private verification question (only YOU will ever see this)
                    <input value={form.verificationQuestion} onChange={e => set("verificationQuestion", e.target.value)}
                           placeholder="e.g. What color is the sticker?" />
                </label>
            )}
            <button type="submit" className="primary">{submitLabel}</button>
        </form>
    );
}