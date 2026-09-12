import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
    const [mode, setMode] = useState("login");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const { login, register } = useAuth();
    const navigate = useNavigate();

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        try {
            if (mode === "login") await login(email, password);
            else await register(name, email, password);
            navigate("/");
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div className="card form-card">
            <div className="tabs">
                <button type="button" className={mode === "login" ? "tab active" : "tab"} onClick={() => setMode("login")}>Login</button>
                <button type="button" className={mode === "register" ? "tab active" : "tab"} onClick={() => setMode("register")}>Register</button>
            </div>
            <form onSubmit={handleSubmit}>
                {mode === "register" && (
                    <label>Name
                        <input value={name} onChange={e => setName(e.target.value)} required />
                    </label>
                )}
                <label>Email
                    <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
                </label>
                <label>Password
                    <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
                </label>
                {error && <p className="error">{error}</p>}
                <button type="submit" className="primary">{mode === "login" ? "Login" : "Register"}</button>
            </form>
        </div>
    );
}