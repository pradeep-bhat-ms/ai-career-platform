import { useState } from "react";
import { loginUser } from "../services/authService";
import { useNavigate, Link } from "react-router-dom";
import "../ResumeAnalyzer.css";


function Login() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await loginUser(form);
localStorage.setItem("fullName", response.data.fullName);
      navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>Sign In</h2>
        <p className="subtitle">Access your AI Career intelligence workspace</p>

        {error && (
          <div style={{ background: "rgba(239, 68, 68, 0.1)", color: "var(--rose-glow)", padding: 10, borderRadius: 8, fontSize: 13, marginBottom: 16 }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email Address</label>
            <input className="custom-input" name="email" type="email" placeholder="name@domain.com" onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input className="custom-input" name="password" type="password" placeholder="••••••••" onChange={handleChange} required />
          </div>

          <button className="neon-btn-primary" type="submit" style={{ width: "100%", justifyContent: "center", marginTop: 8 }} disabled={loading}>
            {loading ? "Authenticating..." : "Sign In"}
          </button>
        </form>

        <div className="auth-footer">
          <p>Don't have an account? <Link to="/register">Create account</Link></p>
          <p><Link to="/forgot-password" style={{ color: "var(--text-muted)", fontSize: 12 }}>Forgot password?</Link></p>
        </div>
      </div>
    </div>
  );
}

export default Login;