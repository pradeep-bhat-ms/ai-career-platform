import { useState } from "react";
import { forgotPassword, resetPassword } from "../services/authService";
import { useNavigate, Link } from "react-router-dom";
import "../ResumeAnalyzer.css";

function ForgotPassword() {
  const [step, setStep] = useState("email");
  const [email, setEmail] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSendOtp = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    setLoading(true);
    try {
      await forgotPassword(email);
      setMessage("OTP sent to your email. Check your inbox.");
      setStep("reset");
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || "Failed to send OTP");
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    setLoading(true);
    try {
      await resetPassword(email, otpCode, newPassword);
      setMessage("Password reset successful! Redirecting to login...");
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || "Failed to reset password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>{step === "email" ? "Reset Password" : "Enter Verification OTP"}</h2>
        <p className="subtitle">
          {step === "email" ? "We will send a 6-digit verification code to your email." : `Enter OTP sent to ${email}`}
        </p>

        {error && <div className="error-banner">{error}</div>}
        {message && <div className="success-banner">{message}</div>}

        {step === "email" ? (
          <form onSubmit={handleSendOtp}>
            <div className="form-group">
              <label>Registered Email Address</label>
              <input
                type="email"
                className="custom-input"
                placeholder="name@domain.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="neon-btn-primary" style={{ width: "100%", marginTop: 8 }} disabled={loading}>
              {loading ? "Sending OTP..." : "Send Verification Code"}
            </button>
          </form>
        ) : (
          <form onSubmit={handleResetPassword}>
            <div className="form-group">
              <label>6-Digit OTP Code</label>
              <input
                type="text"
                className="custom-input"
                placeholder="123456"
                value={otpCode}
                onChange={(e) => setOtpCode(e.target.value)}
                maxLength={6}
                required
              />
            </div>

            <div className="form-group">
              <label>New Password</label>
              <input
                type="password"
                className="custom-input"
                placeholder="••••••••"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="neon-btn-primary" style={{ width: "100%", marginTop: 8 }} disabled={loading}>
              {loading ? "Resetting..." : "Confirm & Reset Password"}
            </button>
          </form>
        )}

        <div className="auth-footer">
          <p><Link to="/login">Back to Sign In</Link></p>
        </div>
      </div>
    </div>
  );
}

export default ForgotPassword;