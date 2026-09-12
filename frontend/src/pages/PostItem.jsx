import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import ItemForm from "../components/ItemForm";

export default function PostItem() {
    const navigate = useNavigate();
    const [error, setError] = useState("");

    async function handleSubmit(values) {
        setError("");
        try {
            const created = await api("/api/items", { method: "POST", body: values });
            navigate(`/items/${created.id}`);
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div className="card form-card wide">
            <h1>Post a Report</h1>
            {error && <p className="error">{error}</p>}
            <ItemForm submitLabel="Publish report" onSubmit={handleSubmit} />
        </div>
    );
}