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
      {/* Profile Banner */}
      <div className="target-role-banner" style={{ background: "rgba(56, 189, 248, 0.03)", borderColor: "rgba(56, 189, 248, 0.2)" }}>
        <div className="banner-left">
          <div className="brand-logo-icon" style={{ width: 28, height: 28, fontSize: 13 }}>⚡</div>
          <div>
            <strong style={{ color: "#fff", fontSize: 13 }}>Candidate Profile Initialized</strong>
            <p style={{ margin: 0, fontSize: 11, color: "var(--text-muted)" }}>Welcome back, {fullName}. All AI Career agents are operational.</p>
          </div>
        </div>
        <Link to="/resume" className="neon-btn-primary" style={{ padding: "6px 14px", fontSize: 12 }}>
          Launch Resume Studio
        </Link>
      </div>

      {/* Cockpit Stats Row */}
      <div className="cockpit-stats-grid">
        <div className="stat-pill-card">
          <div className="stat-pill-header">
            <span>Career Score</span>
            <span style={{ color: "var(--cyan-glow)" }}>↗</span>
          </div>
          <div className="stat-pill-value" style={{ color: "var(--cyan-glow)" }}>85/100</div>
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
          <div className="stat-pill-value" style={{ color: "var(--emerald-glow)" }}>Online</div>
        </div>
      </div>

      {/* Feature Modules Grid */}
      <div className="box-header" style={{ marginTop: 10 }}>
        <h3>Verified Career Intelligence Modules</h3>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: 14 }}>
        <div className="studio-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>📄 Resume Studio</strong>
            <span className="role-pill">ATS Score: 85%</span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Extract atomic skills, run ATS scoring against target roles, and evaluate gaps.
          </p>
          <Link to="/resume" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Analyze Resume →
          </Link>
        </div>

        <div className="studio-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>💼 JD Analyzer</strong>
            <span className="role-pill">Atomic Skills</span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Parse employer requirements into deterministic required and preferred skills.
          </p>
          <Link to="/job-description" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Submit Job Description →
          </Link>
        </div>

        <div className="studio-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <strong style={{ color: "#fff", fontSize: 14 }}>🎯 Match & Compare</strong>
            <span className="role-pill">Gap Engine</span>
          </div>
          <p style={{ color: "var(--text-muted)", fontSize: 12, margin: "0 0 14px 0" }}>
            Evaluate resume suitability against target JDs and generate recommendations.
          </p>
          <Link to="/match" style={{ color: "var(--cyan-glow)", fontSize: 12, textDecoration: "none", fontWeight: 600 }}>
            Compare Artifacts →
          </Link>
        </div>
      </div>
    </AppLayout>
  );
}

export default Dashboard;