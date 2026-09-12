import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Browse from "./pages/Browse";
import ItemDetail from "./pages/ItemDetail";
import PostItem from "./pages/PostItem";
import MyReports from "./pages/MyReports";

function Protected({ children }) {
    const { loggedIn } = useAuth();
    return loggedIn ? children : <Navigate to="/login" />;
}

export default function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Navbar />
                <main className="container">
                    <Routes>
                        <Route path="/" element={<Browse />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/items/:id" element={<ItemDetail />} />
                        <Route path="/post" element={<Protected><PostItem /></Protected>} />
                        <Route path="/mine" element={<Protected><MyReports /></Protected>} />
                    </Routes>
                </main>
            </BrowserRouter>
        </AuthProvider>
    );
}