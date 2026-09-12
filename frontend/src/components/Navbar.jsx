import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
    const { loggedIn, email, logout } = useAuth();
    const navigate = useNavigate();

    return (
        <nav className="navbar">
            <Link to="/" className="brand">Campus Lost & Found</Link>
            <div className="nav-links">
                <Link to="/">Browse</Link>
                {loggedIn ? (
                    <>
                        <Link to="/post">Post Item</Link>
                        <Link to="/mine">My Reports</Link>
                        <span className="nav-email">{email}</span>
                        <button onClick={() => { logout(); navigate("/login"); }}>Logout</button>
                    </>
                ) : (
                    <Link to="/login">Login</Link>
                )}
            </div>
        </nav>
    );
}