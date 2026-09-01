import { useState } from "react";
import { registerUser } from "../services/authService";
import { useNavigate, Link } from "react-router-dom";
import "../ResumeAnalyzer.css";


function Register() {
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await registerUser(form);
 localStorage.setItem("fullName", response.data.fullName);
      navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>Create Account</h2>
        <p className="subtitle">Join the AI Career Intelligence platform</p>

        {error && <div className="error-banner">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full Name</label>
            <input className="custom-input" name="fullName" placeholder="Pradeep Bhat" onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Email Address</label>
            <input className="custom-input" name="email" type="email" placeholder="name@domain.com" onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input className="custom-input" name="password" type="password" placeholder="••••••••" onChange={handleChange} required />
          </div>

          <button className="neon-btn-primary" type="submit" style={{ width: "100%", marginTop: 8 }} disabled={loading}>
            {loading ? "Creating Account..." : "Create Account"}
          </button>
        </form>

        <div className="auth-footer">
          <p>Already have an account? <Link to="/login">Sign In</Link></p>
        </div>
      </div>
    </div>
  );
}

export default Register;