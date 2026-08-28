import { useState } from "react";
import { forgotPassword, resetPassword } from "../services/authService";
import { useNavigate, Link } from "react-router-dom";

function ForgotPassword() {
  const [step, setStep] = useState("email"); // "email" | "reset"
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
      setTimeout(() => navigate("/login"), 2000);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || "Failed to reset password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: "60px auto", padding: 20 }}>
      <h2>{step === "email" ? "Forgot Password" : "Reset Password"}</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      {message && <p style={{ color: "green" }}>{message}</p>}

      {step === "email" ? (
        <form onSubmit={handleSendOtp}>
          <input
            type="email"
            placeholder="Enter your registered email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{ display: "block", width: "100%", marginBottom: 12, padding: 10 }}
          />
          <button type="submit" disabled={loading}>
            {loading ? "Sending..." : "Send OTP"}
          </button>
        </form>
      ) : (
        <form onSubmit={handleResetPassword}>
          <p style={{ fontSize: 13, color: "#666" }}>Enter the OTP sent to {email}</p>
          <input
            type="text"
            placeholder="6-digit OTP"
            value={otpCode}
            onChange={(e) => setOtpCode(e.target.value)}
            required
            maxLength={6}
            style={{ display: "block", width: "100%", marginBottom: 12, padding: 10 }}
          />
          <input
            type="password"
            placeholder="New password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
            style={{ display: "block", width: "100%", marginBottom: 12, padding: 10 }}
          />
          <button type="submit" disabled={loading}>
            {loading ? "Resetting..." : "Reset Password"}
          </button>
          <p style={{ marginTop: 10 }}>
            <button type="button" onClick={() => setStep("email")} style={{ background: "none", border: "none", color: "#4f46e5", cursor: "pointer" }}>
              Didn't get the code? Try again
            </button>
          </p>
        </form>
      )}

      <p style={{ marginTop: 16 }}>
        <Link to="/login">Back to Login</Link>
      </p>
    </div>
  );
}

export default ForgotPassword;