import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Browse from "./pages/Browse";
import ItemDetail from "./pages/ItemDetail";
import PostItem from "./pages/PostItem";
import MyReports from "./pages/MyReports";

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
                        <Route path="/post" element={<PostItem />} />
                        <Route path="/edit/:id" element={<PostItem />} />
                        <Route path="/mine" element={<MyReports />} />
                    </Routes>
                </main>
            </BrowserRouter>
        </AuthProvider>
    );
}