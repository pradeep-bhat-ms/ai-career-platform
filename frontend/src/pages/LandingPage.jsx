import React from "react";
import { Link } from "react-router-dom";
import "../ResumeAnalyzer.css";

export default function LandingPage() {
  return (
    <div style={{ minHeight: "100vh", backgroundColor: "var(--bg-main)", color: "var(--text-primary)" }}>
      {/* Top Navbar */}
      <nav style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "20px 40px", borderBottom: "1px solid var(--border-subtle)" }}>
        <div className="brand-header">
          <div className="brand-logo-icon">C</div>
          <div>
            <div className="brand-title">Career Agents</div>
            <div className="brand-subtitle">AI Career Intelligence</div>
          </div>
        </div>

        <div style={{ display: "flex", gap: 14 }}>
          <Link to="/login" className="btn-secondary" style={{ textDecoration: "none" }}>Sign In</Link>
          <Link to="/register" className="neon-btn-primary" style={{ textDecoration: "none" }}>Get Started Free</Link>
        </div>
      </nav>

      {/* Hero Section */}
      <div style={{ maxWidth: 900, margin: "80px auto", textAlign: "center", padding: "0 20px" }}>
        <span className="role-pill" style={{ marginBottom: 16, display: "inline-block" }}>
          🚀 Powered by Spring AI & PGVector
        </span>
        <h1 style={{ fontSize: 44, fontWeight: 800, margin: "16px 0", lineHeight: 1.2 }}>
          Accelerate Your Tech Career With <br />
          <span style={{ color: "var(--cyan-glow)" }}>AI-Driven Resume & Interview Intelligence</span>
        </h1>
        <p style={{ fontSize: 16, color: "var(--text-secondary)", maxWidth: 650, margin: "0 auto 32px auto", lineHeight: 1.6 }}>
          Extract atomic technical skills from your resume, match against real job descriptions, target critical ATS gaps, and practice with our context-aware RAG interview coach.
        </p>

        <div style={{ display: "flex", justifyContent: "center", gap: 16 }}>
          <Link to="/register" className="neon-btn-primary" style={{ textDecoration: "none", padding: "12px 28px", fontSize: 14 }}>
            Start Analyzing Free →
          </Link>
        </div>

        {/* Feature Cards Grid */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))", gap: 20, marginTop: 64, textAlign: "left" }}>
          <div className="studio-card">
            <div style={{ fontSize: 24, marginBottom: 12 }}>📄</div>
            <h3 style={{ margin: "0 0 8px 0", color: "#fff", fontSize: 15 }}>Resume ATS Intelligence</h3>
            <p style={{ color: "var(--text-muted)", fontSize: 13, margin: 0 }}>Extract technical skills, evaluate role fitness, and detect missing keywords.</p>
          </div>

          <div className="studio-card">
            <div style={{ fontSize: 24, marginBottom: 12 }}>🎯</div>
            <h3 style={{ margin: "0 0 8px 0", color: "#fff", fontSize: 15 }}>Resume ↔ JD Matching</h3>
            <p style={{ color: "var(--text-muted)", fontSize: 13, margin: 0 }}>Deterministic and AI agent matching to pinpoint required vs preferred gaps.</p>
          </div>

          <div className="studio-card">
            <div style={{ fontSize: 24, marginBottom: 12 }}>🤖</div>
            <h3 style={{ margin: "0 0 8px 0", color: "#fff", fontSize: 15 }}>RAG Interview Assistant</h3>
            <p style={{ color: "var(--text-muted)", fontSize: 13, margin: 0 }}>Vector-retrieved mock interview prep based specifically on your projects and JD.</p>
          </div>
        </div>
      </div>
    </div>
  );
}