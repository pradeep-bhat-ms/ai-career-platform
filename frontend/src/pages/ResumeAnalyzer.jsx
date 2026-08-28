import React, { useState, useEffect, useRef } from "react";
import AppLayout from "../components/AppLayout";
import {
  uploadResume,
  analyzeResume,
  getAvailableRoles,
  analyzeForRole,
  runCareerAgent
} from "../services/resumeService";
import "../ResumeAnalyzer.css";

function ResumeAnalyzer() {
  const [file, setFile] = useState(null);
  const [resumeId, setResumeId] = useState(null);
  const [extractedData, setExtractedData] = useState(null);
  const [roles, setRoles] = useState([]);
  const [selectedRole, setSelectedRole] = useState("Software Engineer");
  const [roleAnalysis, setRoleAnalysis] = useState(null);
  const [careerAgentData, setCareerAgentData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [evaluatingRole, setEvaluatingRole] = useState(false);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState("deterministic");
  const [dragActive, setDragActive] = useState(false);

  const fileInputRef = useRef(null);

  useEffect(() => {
    getAvailableRoles()
      .then((res) => {
        if (res.data && res.data.length > 0) {
          setRoles(res.data);
          if (!res.data.includes(selectedRole)) {
            setSelectedRole(res.data[0]);
          }
        }
      })
      .catch(() => setError("Could not load target roles"));
  }, []);

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setFile(e.dataTransfer.files[0]);
      setError("");
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
      setError("");
    }
  };

  // Explicit Trigger: Upload file, run extraction, and evaluate against target role
  const handleUploadAndAnalyze = async () => {
    if (!file) {
      setError("Please select or drop a PDF resume first.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      // 1. Upload PDF
      const uploadRes = await uploadResume(file);
      const newResumeId = uploadRes.data.resumeId;
      setResumeId(newResumeId);

      // 2. Extract Data
      const analyzeRes = await analyzeResume(newResumeId);
      setExtractedData(analyzeRes.data.extractedData);

      // 3. Run Deterministic and Agent evaluations
      if (selectedRole) {
        const [deterministicRes, agentRes] = await Promise.all([
          analyzeForRole(newResumeId, selectedRole),
          runCareerAgent(newResumeId, selectedRole)
        ]);
        setRoleAnalysis(deterministicRes.data);
        setCareerAgentData(agentRes.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || "Analysis failed. Please check backend logs.");
    } finally {
      setLoading(false);
    }
  };

  // Re-run evaluation when switching target role on an already uploaded resume
  const handleRoleReEvaluate = async () => {
    if (!resumeId || !selectedRole) return;
    setEvaluatingRole(true);
    setError("");

    try {
      const [deterministicRes, agentRes] = await Promise.all([
        analyzeForRole(resumeId, selectedRole),
        runCareerAgent(resumeId, selectedRole)
      ]);
      setRoleAnalysis(deterministicRes.data);
      setCareerAgentData(agentRes.data);
    } catch (err) {
      setError(err.response?.data?.message || "Role evaluation failed");
    } finally {
      setEvaluatingRole(false);
    }
  };

  const handleReset = () => {
    setFile(null);
    setResumeId(null);
    setExtractedData(null);
    setRoleAnalysis(null);
    setCareerAgentData(null);
    setError("");
  };

  return (
    <AppLayout
      title="Resume Studio"
      subtitle="ATS analysis, bullet optimization, keyword intelligence"
    >
      {error && <div className="error-banner">{error}</div>}

      {/* --- SECTION 1: UPLOAD & TARGET ROLE CARD (ALWAYS VISIBLE AT TOP) --- */}
      <div className="studio-card highlight" style={{ maxWidth: 880, margin: "0 auto 24px auto" }}>
        <div className="persona-card" style={{ marginBottom: 16 }}>
          <div className="persona-badge-title">
            <span>◎</span> TARGET ROLE SELECTION
          </div>
          <div>
            <label className="persona-label">Select Target Job Role</label>
            <select
              className="persona-select"
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
            >
              {roles.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Drag & Drop Area */}
        <div
          className={`dropzone-container ${dragActive ? "drag-active" : ""}`}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current.click()}
        >
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf"
            style={{ display: "none" }}
            onChange={handleFileChange}
          />

          <div className="dropzone-icon">📄</div>
          <div className="dropzone-main-text">
            {file ? file.name : "Drag & drop your resume PDF here"}
          </div>
          <div className="dropzone-sub-text">
            {file ? `${(file.size / 1024).toFixed(1)} KB selected` : "PDF format supported"}
          </div>

          <button
            type="button"
            className="btn-browse"
            onClick={(e) => {
              e.stopPropagation();
              fileInputRef.current.click();
            }}
          >
            {file ? "Change File" : "Browse Files"}
          </button>
        </div>

        {/* Explicit Action Button */}
        <div style={{ textAlign: "center", marginTop: 20 }}>
          <button
            className="neon-btn-primary"
            style={{ minWidth: 260, padding: "12px 28px", fontSize: 14 }}
            onClick={handleUploadAndAnalyze}
            disabled={loading || !file}
          >
            {loading && <span className="spinner"></span>}
            {loading ? "Analyzing Resume with AI..." : "Upload & Analyze Resume"}
          </button>
        </div>
      </div>

      {/* --- SECTION 2: RESULTS CARD (APPEARS BELOW UPON ANALYSIS) --- */}
      {roleAnalysis && (
        <div style={{ maxWidth: 880, margin: "0 auto" }}>
          {/* Target Role Status Header */}
          <div className="target-role-banner">
            <div className="banner-left">
              <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>Evaluated Target Role:</span>
              <span className="role-pill">{selectedRole}</span>
            </div>

            <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
              <select
                className="persona-select"
                value={selectedRole}
                onChange={(e) => setSelectedRole(e.target.value)}
                style={{ width: 220 }}
              >
                {roles.map((r) => (
                  <option key={r} value={r}>{r}</option>
                ))}
              </select>

              <button
                className="neon-btn-primary"
                onClick={handleRoleReEvaluate}
                disabled={evaluatingRole}
              >
                {evaluatingRole ? "Evaluating..." : "Re-evaluate"}
              </button>

              <button className="btn-secondary" onClick={handleReset}>
                Clear
              </button>
            </div>
          </div>

          {/* Score Overview Row */}
          <div className="score-overview-grid">
            <div className="hero-score-card">
              <div className="hero-score-number">{roleAnalysis.matchPercentage}%</div>
              <div className="hero-score-label">ATS Score</div>
            </div>

            <div className="section-checks-grid">
              <div className="check-item-box passed">✓ Experience</div>
              <div className="check-item-box passed">✓ Education</div>
              <div className={`check-item-box ${roleAnalysis.missingRequiredSkills?.length > 0 ? "failed" : "passed"}`}>
                {roleAnalysis.missingRequiredSkills?.length > 0 ? "✕" : "✓"} Skills Coverage
              </div>
              <div className="check-item-box passed">✓ Projects</div>
              <div className="check-item-box passed">✓ Summary</div>
            </div>
          </div>

          {/* Styled Navigation Tabs */}
          <div className="studio-tab-bar">
            <button
              className={`studio-tab-btn ${activeTab === "deterministic" ? "active" : ""}`}
              onClick={() => setActiveTab("deterministic")}
            >
              <span>📊</span> ATS Role Match
            </button>
            <button
              className={`studio-tab-btn ${activeTab === "agent" ? "active" : ""}`}
              onClick={() => setActiveTab("agent")}
            >
              <span>🤖</span> Career Skill Agent
            </button>
          </div>

          {/* TAB 1: Deterministic Match View */}
          {activeTab === "deterministic" && (
            <div className="diagnostics-grid">
              <div className="studio-card">
                <div className="box-header">
                  <h3>Missing Required Skills</h3>
                  <span className="badge-count red">{roleAnalysis.missingRequiredSkills?.length || 0}</span>
                </div>
                <p style={{ fontSize: 12, color: "var(--text-muted)", margin: "0 0 10px 0" }}>
                  Add these core skills to your resume to pass ATS filters
                </p>
                <div className="tag-collection">
                  {!roleAnalysis.missingRequiredSkills || roleAnalysis.missingRequiredSkills.length === 0 ? (
                    <span className="custom-chip green">All core skills covered!</span>
                  ) : (
                    roleAnalysis.missingRequiredSkills.map((s) => (
                      <span key={s} className="custom-chip red">{s}</span>
                    ))
                  )}
                </div>
              </div>

              <div className="studio-card">
                <div className="box-header">
                  <h3>Recommended to Learn</h3>
                  <span className="badge-count">{roleAnalysis.missingRecommendedSkills?.length || 0}</span>
                </div>
                <p style={{ fontSize: 12, color: "var(--text-muted)", margin: "0 0 10px 0" }}>
                  Secondary skills to boost candidate competitiveness
                </p>
                <div className="tag-collection">
                  {roleAnalysis.missingRecommendedSkills?.map((s) => (
                    <span key={s} className="custom-chip amber">{s}</span>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* TAB 2: Career Agent Recommendations */}
          {activeTab === "agent" && careerAgentData && (
            <div className="studio-card">
              <div className="box-header">
                <h3>🎯 Agent Recommendations</h3>
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: 10, marginTop: 12 }}>
                {careerAgentData.recommendedNextSkills?.map((item, idx) => (
                  <div key={idx} className="check-item-box" style={{ justifyContent: "space-between", background: "var(--bg-main)" }}>
                    <div>
                      <strong style={{ color: "#fff", display: "block" }}>{item.skill}</strong>
                      <span style={{ fontSize: 12, color: "var(--text-muted)" }}>{item.reason}</span>
                    </div>
                    <span className={`custom-chip ${item.priority === "High" ? "red" : "amber"}`}>
                      {item.priority} Priority
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </AppLayout>
  );
}

export default ResumeAnalyzer;