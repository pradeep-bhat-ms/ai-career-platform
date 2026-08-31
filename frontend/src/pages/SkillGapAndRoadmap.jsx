import React, { useState, useEffect } from "react";
import AppLayout from "../components/AppLayout";
import { getMyResumes } from "../services/resumeService";
import { getSkillGap, generateLearningPlan } from "../services/skillGapService";
import "../ResumeAnalyzer.css";

function SkillGapAndRoadmap() {
  const [resumes, setResumes] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState("");
  const [targetRole, setTargetRole] = useState("Java Full Stack Developer");

  const [skillGap, setSkillGap] = useState(null);
  const [plan, setPlan] = useState(null);

  const [loadingGap, setLoadingGap] = useState(false);
  const [loadingPlan, setLoadingPlan] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    getMyResumes()
      .then((res) => {
        const list = res.data || [];
        setResumes(list);
        if (list.length > 0) {
          setSelectedResumeId(list[0].id);
        }
      })
      .catch(() => setError("Could not load resumes."));
  }, []);

  const handleCheckGap = async () => {
    if (!selectedResumeId || !targetRole.trim()) return;
    setLoadingGap(true);
    setError("");
    setPlan(null);
    try {
      const res = await getSkillGap(selectedResumeId, targetRole.trim());
      setSkillGap(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to compute skill gap.");
    } finally {
      setLoadingGap(false);
    }
  };

  const handleGeneratePlan = async () => {
    if (!selectedResumeId || !targetRole.trim()) return;
    setLoadingPlan(true);
    setError("");
    try {
      const res = await generateLearningPlan(selectedResumeId, targetRole.trim());
      setPlan(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to generate learning plan.");
    } finally {
      setLoadingPlan(false);
    }
  };

  const getStatusChipClass = (status) => {
    switch (status) {
      case "Strong":
        return "green";
      case "Medium":
        return "blue";
      case "Weak":
        return "amber";
      case "Missing":
      default:
        return "red";
    }
  };

  return (
    <AppLayout
      title="Skill Gap & Learning Roadmap"
      subtitle="Identify missing technical competencies and generate targeted AI study roadmaps"
    >
      {error && <div className="error-banner">{error}</div>}

      <div style={{ maxWidth: 960, margin: "0 auto", display: "flex", flexDirection: "column", gap: 16 }}>
        
        {/* INPUT SELECTION CARD */}
        <div className="studio-card highlight">
          <div className="box-header" style={{ marginBottom: 14 }}>
            <h3>🎯 Target Role & Resume Selection</h3>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
            <div className="form-group">
              <label>Select Analyzed Resume *</label>
              <select
                className="custom-select"
                value={selectedResumeId}
                onChange={(e) => setSelectedResumeId(e.target.value)}
              >
                <option value="">-- Choose a resume --</option>
                {resumes.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.fileName || `Resume #${r.id}`}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Target Role *</label>
              <input
                type="text"
                className="custom-input"
                value={targetRole}
                onChange={(e) => setTargetRole(e.target.value)}
                placeholder="e.g. Java Full Stack Developer"
              />
            </div>
          </div>

          <button
            className="neon-btn-primary"
            style={{ width: "100%", marginTop: 8, padding: "12px" }}
            onClick={handleCheckGap}
            disabled={loadingGap || !selectedResumeId || !targetRole.trim()}
          >
            {loadingGap && <span className="spinner"></span>}
            {loadingGap ? "Evaluating Skill Matrix..." : "📊 Check Skill Gap"}
          </button>
        </div>

        {/* SKILL GAP BREAKDOWN */}
        {skillGap && (
          <div className="studio-card highlight">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
              <div>
                <h3 style={{ margin: "0 0 4px 0", color: "#fff", fontSize: 16 }}>
                  {skillGap.targetRole} — Competency Matrix
                </h3>
                <span style={{ fontSize: 12, color: "var(--text-muted)" }}>
                  Matched against verified required and recommended skills
                </span>
              </div>
              <span className="badge-count blue">{skillGap.skillGaps?.length || 0} Skills Analyzed</span>
            </div>

            <div style={{ display: "flex", flexWrap: "wrap", gap: 8, marginTop: 12 }}>
              {skillGap.skillGaps?.map((s) => (
                <div
                  key={s.skillName}
                  className="check-item-box"
                  style={{
                    display: "inline-flex",
                    alignItems: "center",
                    gap: 8,
                    padding: "6px 12px",
                    background: "var(--bg-main)"
                  }}
                >
                  <span style={{ color: "#fff", fontSize: 13, fontWeight: 600 }}>{s.skillName}</span>
                  <span className={`custom-chip ${getStatusChipClass(s.status)}`} style={{ fontSize: 10 }}>
                    {s.status}
                  </span>
                </div>
              ))}
            </div>

            <button
              className="neon-btn-primary"
              style={{
                width: "100%",
                marginTop: 20,
                padding: "12px",
                background: "rgba(56, 189, 248, 0.2)",
                borderColor: "var(--cyan-glow)"
              }}
              onClick={handleGeneratePlan}
              disabled={loadingPlan}
            >
              {loadingPlan && <span className="spinner"></span>}
              {loadingPlan ? "Synthesizing AI Curriculum..." : "⚡ Generate 4-Week Learning Roadmap"}
            </button>
          </div>
        )}

        {/* 4-WEEK LEARNING ROADMAP */}
        {plan && (
          <div className="studio-card highlight">
            <div className="box-header" style={{ marginBottom: 14 }}>
              <h3>📅 4-Week Targeted Roadmap: {plan.targetRole}</h3>
              <span className="role-pill" style={{ color: "var(--emerald-glow)" }}>AI Generated</span>
            </div>

            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {plan.items?.map((item, idx) => (
                <div
                  key={idx}
                  className="interview-review-box"
                  style={{
                    background: "var(--bg-main)",
                    borderLeft: "4px solid var(--cyan-glow)"
                  }}
                >
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 6 }}>
                    <strong style={{ color: "#fff", fontSize: 14 }}>
                      Week {item.weekNumber}: {item.topic}
                    </strong>
                    <span className="custom-chip blue" style={{ fontSize: 10 }}>
                      Week {item.weekNumber} Milestone
                    </span>
                  </div>
                  <p style={{ fontSize: 12, color: "var(--text-secondary)", margin: 0, lineHeight: 1.5 }}>
                    <strong style={{ color: "var(--cyan-glow)" }}>Suggested Approach: </strong>
                    {item.resourceSuggestion}
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}

      </div>
    </AppLayout>
  );
}

export default SkillGapAndRoadmap;