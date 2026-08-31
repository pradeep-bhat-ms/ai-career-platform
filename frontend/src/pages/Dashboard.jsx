import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../components/AppLayout";
import { getMyResumes } from "../services/resumeService";
import { getMyJobDescriptions } from "../services/jobDescriptionService";
import "../ResumeAnalyzer.css";

function Dashboard() {
  const fullName = localStorage.getItem("fullName") || "Candidate";
  const [resumes, setResumes] = useState([]);
  const [jds, setJds] = useState([]);

  useEffect(() => {
    Promise.all([getMyResumes(), getMyJobDescriptions()])
      .then(([resumeRes, jdRes]) => {
        setResumes(resumeRes.data || []);
        setJds(jdRes.data || []);
      })
      .catch(() => {});
  }, []);

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

      {/* Cockpit Stats Row */}
      <div className="cockpit-stats-grid">
        <div className="stat-pill-card">
          <div className="stat-pill-header">
            <span>Career Score</span>
            <span style={{ color: "var(--cyan-glow)" }}>↗</span>
          </div>
          <div
            className="stat-pill-value"
            style={{ color: "var(--cyan-glow)" }}
          >
            85/100
          </div>
        </div>

        <div className="stat-pill-card">
          <div className="stat-pill-header">
            <span>Active Artifacts</span>
            <span>📄</span>
          </div>
          <div className="stat-pill-value">{resumes.length} Resumes</div>
        </div>

        <div className="stat-pill-card">
          <div className="stat-pill-header">
            <span>Target Roles</span>
            <span>💼</span>
          </div>
          <div className="stat-pill-value">{jds.length} JDs</div>
        </div>

        <div className="stat-pill-card">
          <div className="stat-pill-header">
            <span>AI Modules</span>
            <span style={{ color: "var(--emerald-glow)" }}>●</span>
          </div>
          <div
            className="stat-pill-value"
            style={{ color: "var(--emerald-glow)" }}
          >
            Online
          </div>
        </div>
      </div>

      {/* Verified Feature Modules Heading */}
      <div className="box-header" style={{ marginTop: 14 }}>
        <h3>Verified Career Intelligence Modules</h3>
      </div>

      {/* Modular Card Grid */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
          gap: 16
        }}
      >
        {/* Card 1: Resume Studio */}
        <div className="studio-card">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 8
            }}
          >
            <strong style={{ color: "#fff", fontSize: 14 }}>
              📄 Resume Studio
            </strong>
            <span className="role-pill">ATS Score: 85%</span>
          </div>
          <p
            style={{
              color: "var(--text-muted)",
              fontSize: 12,
              margin: "0 0 14px 0"
            }}
          >
            Extract atomic skills, run ATS scoring against target roles, and
            manage documents.
          </p>
          <Link
            to="/resume"
            style={{
              color: "var(--cyan-glow)",
              fontSize: 12,
              textDecoration: "none",
              fontWeight: 600
            }}
          >
            Analyze Resume →
          </Link>
        </div>

        {/* Card 2: JD Analyzer */}
        <div className="studio-card">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 8
            }}
          >
            <strong style={{ color: "#fff", fontSize: 14 }}>
              💼 JD Analyzer
            </strong>
            <span className="role-pill">Atomic Skills</span>
          </div>
          <p
            style={{
              color: "var(--text-muted)",
              fontSize: 12,
              margin: "0 0 14px 0"
            }}
          >
            Parse employer requirements into deterministic required and
            preferred skills.
          </p>
          <Link
            to="/job-description"
            style={{
              color: "var(--cyan-glow)",
              fontSize: 12,
              textDecoration: "none",
              fontWeight: 600
            }}
          >
            Submit Job Description →
          </Link>
        </div>

        {/* Card 3: Match & Compare */}
        <div className="studio-card">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 8
            }}
          >
            <strong style={{ color: "#fff", fontSize: 14 }}>
              🎯 Match & Compare
            </strong>
            <span className="role-pill">Gap Engine</span>
          </div>
          <p
            style={{
              color: "var(--text-muted)",
              fontSize: 12,
              margin: "0 0 14px 0"
            }}
          >
            Evaluate resume compatibility against target JDs and inspect match scoring.
          </p>
          <Link
            to="/match"
            style={{
              color: "var(--cyan-glow)",
              fontSize: 12,
              textDecoration: "none",
              fontWeight: 600
            }}
          >
            Compare Artifacts →
          </Link>
        </div>

        {/* Card 4: Skill Roadmap */}
        <div className="studio-card highlight">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 8
            }}
          >
            <strong style={{ color: "#fff", fontSize: 14 }}>
              🗺️ Skill Roadmap
            </strong>
            <span
              className="role-pill"
              style={{
                background: "rgba(56, 189, 248, 0.15)",
                color: "var(--cyan-glow)",
                borderColor: "rgba(56, 189, 248, 0.3)"
              }}
            >
              AI Curriculum
            </span>
          </div>
          <p
            style={{
              color: "var(--text-muted)",
              fontSize: 12,
              margin: "0 0 14px 0"
            }}
          >
            Compute missing skills against role benchmarks and generate personalized 4-week study plans.
          </p>
          <Link
            to="/skill-gap"
            style={{
              color: "var(--cyan-glow)",
              fontSize: 12,
              textDecoration: "none",
              fontWeight: 600
            }}
          >
            Launch Skill Roadmap →
          </Link>
        </div>

        {/* Card 5: AI Interview Agent */}
        <div className="studio-card">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 8
            }}
          >
            <strong style={{ color: "#fff", fontSize: 14 }}>
              🎙 AI Interview Agent
            </strong>
            <span
              className="role-pill"
              style={{
                background: "rgba(251, 191, 36, 0.15)",
                color: "var(--amber-glow)",
                borderColor: "rgba(251, 191, 36, 0.3)"
              }}
            >
              Interactive Mock
            </span>
          </div>
          <p
            style={{
              color: "var(--text-muted)",
              fontSize: 12,
              margin: "0 0 14px 0"
            }}
          >
            Real-time dynamic questioning, answer evaluation, strength breakdown, and instant scoring.
          </p>
          <Link
            to="/interview"
            style={{
              color: "var(--cyan-glow)",
              fontSize: 12,
              textDecoration: "none",
              fontWeight: 600
            }}
          >
            Launch Interview Agent →
          </Link>
        </div>

        {/* Card 6: Code Arena */}
        <div className="studio-card">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 8
            }}
          >
            <strong style={{ color: "#fff", fontSize: 14 }}>
              💻 Code Arena
            </strong>
            <span
              className="role-pill"
              style={{
                background: "rgba(56, 189, 248, 0.15)",
                color: "var(--cyan-glow)",
                borderColor: "rgba(56, 189, 248, 0.3)"
              }}
            >
              Multi-Language
            </span>
          </div>
          <p
            style={{
              color: "var(--text-muted)",
              fontSize: 12,
              margin: "0 0 14px 0"
            }}
          >
            Solve algorithmic problems in Java, Python, C++, JS, or SQL with instant AI code evaluation.
          </p>
          <Link
            to="/code-arena"
            style={{
              color: "var(--cyan-glow)",
              fontSize: 12,
              textDecoration: "none",
              fontWeight: 600
            }}
          >
            Launch Code Arena →
          </Link>
        </div>

        {/* Card 7: RAG Interview Prep Assistant */}
        <div className="studio-card">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 8
            }}
          >
            <strong style={{ color: "#fff", fontSize: 14 }}>
              🤖 RAG Prep Assistant
            </strong>
            <span
              className="role-pill"
              style={{
                background: "rgba(52, 211, 153, 0.15)",
                color: "var(--emerald-glow)",
                borderColor: "rgba(52, 211, 153, 0.3)"
              }}
            >
              PGVector AI
            </span>
          </div>
          <p
            style={{
              color: "var(--text-muted)",
              fontSize: 12,
              margin: "0 0 14px 0"
            }}
          >
            Vector similarity Q&A grounded directly in your uploaded notes and
            technical documents.
          </p>
          <Link
            to="/rag-assistant"
            style={{
              color: "var(--cyan-glow)",
              fontSize: 12,
              textDecoration: "none",
              fontWeight: 600
            }}
          >
            Launch Assistant →
          </Link>
        </div>
      </div>
    </AppLayout>
  );
}

export default Dashboard;