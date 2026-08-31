import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../components/AppLayout";
import { getDashboardSummary } from "../services/dashboardService";
import "../ResumeAnalyzer.css";

function Dashboard() {
  const fullName = localStorage.getItem("fullName") || "Candidate";
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDashboardSummary()
      .then((res) => {
        setSummary(res.data);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const totalGaps = summary?.skillGapBreakdown
    ? Object.values(summary.skillGapBreakdown).reduce((a, b) => a + b, 0)
    : 0;

  return (
    <AppLayout
      title="Career Cockpit Dashboard"
      subtitle="AI Career Operating System & Telemetry Controls"
    >
      {/* Profile Status Banner */}
      <div
        className="target-role-banner"
        style={{
          background: "rgba(56, 189, 248, 0.03)",
          borderColor: "rgba(56, 189, 248, 0.2)"
        }}
      >
        <div className="banner-left">
          <div
            className="brand-logo-icon"
            style={{ width: 28, height: 28, fontSize: 13 }}
          >
            ⚡
          </div>
          <div>
            <strong style={{ color: "#fff", fontSize: 13 }}>
              Candidate Profile Initialized
            </strong>
            <p style={{ margin: 0, fontSize: 11, color: "var(--text-muted)" }}>
              Welcome back, {fullName}. All AI Career agents are operational.
            </p>
          </div>
        </div>
        <Link
          to="/interview"
          className="neon-btn-primary"
          style={{ padding: "6px 14px", fontSize: 12 }}
        >
          🎙 Start Mock Interview
        </Link>
      </div>

      {/* Dynamic Telemetry Stats Row */}
      <div className="cockpit-stats-grid">
        <div className="stat-pill-card">
          <div className="stat-pill-header">
            <span>Average Mock Score</span>
            <span style={{ color: "var(--cyan-glow)" }}>↗</span>
          </div>
          <div
            className="stat-pill-value"
            style={{ color: "var(--cyan-glow)" }}
          >
            {summary?.averageInterviewScore !== null && summary?.averageInterviewScore !== undefined
              ? `${summary.averageInterviewScore.toFixed(1)} pts`
              : "N/A"}
          </div>
        </div>

        <div className="stat-pill-card">
          <div className="stat-pill-header">
            <span>Active Artifacts</span>
            <span>📄</span>
          </div>
          <div className="stat-pill-value">
            {summary ? `${summary.resumeCount} Resumes` : "..."}
          </div>
        </div>

        <div className="stat-pill-card">
          <div className="stat-pill-header">
            <span>Target Roles</span>
            <span>💼</span>
          </div>
          <div className="stat-pill-value">
            {summary ? `${summary.jobDescriptionCount} JDs` : "..."}
          </div>
        </div>

        <div className="stat-pill-card">
          <div className="stat-pill-header">
            <span>Completed Mock Drills</span>
            <span style={{ color: "var(--emerald-glow)" }}>●</span>
          </div>
          <div
            className="stat-pill-value"
            style={{ color: "var(--emerald-glow)" }}
          >
            {summary ? `${summary.completedInterviewCount} Sessions` : "..."}
          </div>
        </div>
      </div>

      {/* Live Skill Gap Telemetry Banner */}
      {summary?.skillGapBreakdown && totalGaps > 0 && (
        <div
          className="studio-card"
          style={{
            marginTop: 16,
            marginBottom: 16,
            background: "rgba(15, 23, 42, 0.6)",
            borderColor: "rgba(56, 189, 248, 0.2)"
          }}
        >
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 13 }}>
              🎯 Live Competency & Skill Gap Telemetry
            </strong>
            <Link to="/skill-gap" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none" }}>
              View Full Roadmap →
            </Link>
          </div>

          <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
            <span className="custom-chip green">Strong: {summary.skillGapBreakdown.Strong || 0}</span>
            <span className="custom-chip blue">Medium: {summary.skillGapBreakdown.Medium || 0}</span>
            <span className="custom-chip amber">Weak: {summary.skillGapBreakdown.Weak || 0}</span>
            <span className="custom-chip red">Missing: {summary.skillGapBreakdown.Missing || 0}</span>
          </div>
        </div>
      )}

      {/* Verified Feature Modules Heading */}
      <div className="box-header" style={{ marginTop: 14 }}>
        <h3>Verified Career Intelligence Modules</h3>
      </div>

      {/* Modular 7-Card Grid */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
          gap: 16
        }}
      >
        {/* Card 1: Resume Studio */}
        <div className="studio-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>📄 Resume Studio</strong>
            <span className="role-pill">{summary?.resumeCount || 0} Active</span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Extract atomic skills, run ATS scoring against target roles, and manage documents.
          </p>
          <Link to="/resume" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Analyze Resume →
          </Link>
        </div>

        {/* Card 2: JD Analyzer */}
        <div className="studio-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>💼 JD Analyzer</strong>
            <span className="role-pill">{summary?.jobDescriptionCount || 0} Tracked</span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Parse employer requirements into deterministic required and preferred skills.
          </p>
          <Link to="/job-description" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Submit Job Description →
          </Link>
        </div>

        {/* Card 3: Match & Compare */}
        <div className="studio-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>🎯 Match & Compare</strong>
            <span className="role-pill">Gap Engine</span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Evaluate resume compatibility against target JDs and inspect match scoring.
          </p>
          <Link to="/match" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Compare Artifacts →
          </Link>
        </div>

        {/* Card 4: Skill Roadmap */}
        <div className="studio-card highlight">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>🗺️ Skill Roadmap</strong>
            <span className="role-pill" style={{ color: "var(--cyan-glow)" }}>
              {summary?.skillGapBreakdown?.Missing || 0} Gaps Flagged
            </span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Compute missing skills against role benchmarks and generate personalized 4-week study plans.
          </p>
          <Link to="/skill-gap" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Launch Skill Roadmap →
          </Link>
        </div>

        {/* Card 5: AI Interview Agent */}
        <div className="studio-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>🎙 AI Interview Agent</strong>
            <span className="role-pill" style={{ color: "var(--amber-glow)" }}>
              {summary?.completedInterviewCount || 0} Drills Done
            </span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Real-time dynamic questioning, answer evaluation, strength breakdown, and instant scoring.
          </p>
          <Link to="/interview" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Launch Interview Agent →
          </Link>
        </div>

        {/* Card 6: Code Arena */}
        <div className="studio-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>💻 Code Arena</strong>
            <span className="role-pill" style={{ color: "var(--cyan-glow)" }}>Compiler AI</span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Solve algorithmic problems in Java, Python, C++, JS, or SQL with instant AI code evaluation.
          </p>
          <Link to="/code-arena" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Launch Code Arena →
          </Link>
        </div>

        {/* Card 7: RAG Interview Prep Assistant */}
        <div className="studio-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>🤖 RAG Prep Assistant</strong>
            <span className="role-pill" style={{ color: "var(--emerald-glow)" }}>PGVector AI</span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Vector similarity Q&A grounded directly in your uploaded notes and technical documents.
          </p>
          <Link to="/rag-assistant" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Launch Assistant →
          </Link>
        </div>
      </div>
    </AppLayout>
  );
}

export default Dashboard;