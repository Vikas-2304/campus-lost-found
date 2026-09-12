import { createContext, useContext, useState } from "react";
import { api } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [token, setToken] = useState(localStorage.getItem("token"));
    const [email, setEmail] = useState(localStorage.getItem("email"));

    async function login(email, password) {
        const data = await api("/api/auth/login", { method: "POST", body: { email, password } });
        localStorage.setItem("token", data.token);
        localStorage.setItem("email", email);
        setToken(data.token);
        setEmail(email);
    }

    async function register(name, email, password) {
        const data = await api("/api/auth/register", { method: "POST", body: { name, email, password } });
        localStorage.setItem("token", data.token);
        localStorage.setItem("email", email);
        setToken(data.token);
        setEmail(email);
    }

    function logout() {
        localStorage.removeItem("token");
        localStorage.removeItem("email");
        setToken(null);
        setEmail(null);
    }

    return (
        <AuthContext.Provider value={{ token, email, loggedIn: !!token, login, register, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}