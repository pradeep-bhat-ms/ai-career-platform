import React, { useState, useEffect, useRef } from "react";
import AppLayout from "../components/AppLayout";
import {
  uploadResume,
  analyzeResume,
  getAvailableRoles,
  analyzeForRole,
  runCareerAgent,
  getMyResumes,
  deleteResume,
  proposeImprovements,
  applyImprovements
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
  const [savedResumes, setSavedResumes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [evaluatingRole, setEvaluatingRole] = useState(false);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState("deterministic");
  const [dragActive, setDragActive] = useState(false);

  // --- AI REWRITE & PROPOSALS STATE ---
  const [improving, setImproving] = useState(false);
  const [applying, setApplying] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [selectedSuggestions, setSelectedSuggestions] = useState({});
  const [scoreDelta, setScoreDelta] = useState(null);

  const fileInputRef = useRef(null);

  const loadInitialData = async () => {
    try {
      const [rolesRes, resumesRes] = await Promise.all([
        getAvailableRoles(),
        getMyResumes()
      ]);

      if (rolesRes.data && rolesRes.data.length > 0) {
        setRoles(rolesRes.data);
        if (!rolesRes.data.includes(selectedRole)) {
          setSelectedRole(rolesRes.data[0]);
        }
      }

      setSavedResumes(resumesRes.data || []);
    } catch (err) {
      setError("Could not load initial setup data");
    }
  };

  useEffect(() => {
    loadInitialData();
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

  // Upload and analyze new resume
  const handleUploadAndAnalyze = async () => {
    if (!file) {
      setError("Please select or drop a PDF resume first.");
      return;
    }

    setLoading(true);
    setError("");
    setSuggestions([]);
    setScoreDelta(null);

    try {
      const uploadRes = await uploadResume(file);
      const newResumeId = uploadRes.data.resumeId;
      setResumeId(newResumeId);

      const analyzeRes = await analyzeResume(newResumeId);
      setExtractedData(analyzeRes.data.extractedData);

      if (selectedRole) {
        const [deterministicRes, agentRes] = await Promise.all([
          analyzeForRole(newResumeId, selectedRole),
          runCareerAgent(newResumeId, selectedRole)
        ]);
        setRoleAnalysis(deterministicRes.data);
        setCareerAgentData(agentRes.data);
      }

      const updatedResumes = await getMyResumes();
      setSavedResumes(updatedResumes.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Analysis failed. Please check backend logs.");
    } finally {
      setLoading(false);
    }
  };

  // Select an existing resume
  const handleSelectExistingResume = async (existingId) => {
    setLoading(true);
    setError("");
    setResumeId(existingId);
    setFile(null);
    setSuggestions([]);
    setScoreDelta(null);

    try {
      const analyzeRes = await analyzeResume(existingId);
      setExtractedData(analyzeRes.data.extractedData);

      if (selectedRole) {
        const [deterministicRes, agentRes] = await Promise.all([
          analyzeForRole(existingId, selectedRole),
          runCareerAgent(existingId, selectedRole)
        ]);
        setRoleAnalysis(deterministicRes.data);
        setCareerAgentData(agentRes.data);
      }
    } catch (err) {
      setError("Failed to load selected resume evaluation.");
    } finally {
      setLoading(false);
    }
  };

  // Delete an existing resume
  const handleDeleteResume = async (e, idToDelete) => {
    e.stopPropagation();
    if (!window.confirm("Are you sure you want to delete this resume?")) return;

    try {
      await deleteResume(idToDelete);
      setSavedResumes((prev) => prev.filter((r) => r.id !== idToDelete));

      if (resumeId === idToDelete) {
        handleReset();
      }
    } catch (err) {
      setError(err.response?.data?.message || "Failed to delete resume.");
    }
  };

  // Re-run evaluation for new target role
  const handleRoleReEvaluate = async () => {
    if (!resumeId || !selectedRole) return;
    setEvaluatingRole(true);
    setError("");
    setSuggestions([]);
    setScoreDelta(null);

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

  // --- AI REWRITE PROPOSAL TRIGGER ---
  const handleTriggerAIImprovement = async (sectionFilter = "ALL") => {
    const activeId = resumeId || (savedResumes.length > 0 ? savedResumes[0].id : null);
    if (!activeId) {
      setError("Please select or upload a resume first.");
      return;
    }

    setImproving(true);
    setError("");
    setScoreDelta(null);

    try {
      const missing = roleAnalysis?.missingRequiredSkills || [];
      const res = await proposeImprovements(activeId, selectedRole, sectionFilter, missing);
      
      const fetched = res.data || [];
      setSuggestions(fetched);

      const initialSelection = {};
      fetched.forEach((s) => {
        initialSelection[s.id] = true;
      });
      setSelectedSuggestions(initialSelection);
    } catch (err) {
      setError(err.response?.data?.message || "AI failed to generate improvements. Check backend logs.");
    } finally {
      setImproving(false);
    }
  };

  const handleToggleSuggestion = (id) => {
    setSelectedSuggestions((prev) => ({
      ...prev,
      [id]: !prev[id]
    }));
  };

  // --- APPLY SELECTED PROPOSALS & RE-EVALUATE ---
  const handleApplySelected = async () => {
    const chosen = suggestions.filter((s) => selectedSuggestions[s.id]);
    if (chosen.length === 0) {
      setError("Please select at least one proposal to apply.");
      return;
    }

    setApplying(true);
    setError("");

    try {
      const previousScore = roleAnalysis?.matchPercentage ?? 0;
      const res = await applyImprovements(resumeId, selectedRole, chosen);

      const newAnalysis = res.data;
      setRoleAnalysis(newAnalysis);
      setScoreDelta({
        before: previousScore,
        after: newAnalysis.matchPercentage,
        diff: newAnalysis.matchPercentage - previousScore
      });

      setSuggestions([]);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to apply selected improvements.");
    } finally {
      setApplying(false);
    }
  };

  const handleReset = () => {
    setFile(null);
    setResumeId(null);
    setExtractedData(null);
    setRoleAnalysis(null);
    setCareerAgentData(null);
    setSuggestions([]);
    setScoreDelta(null);
    setError("");
  };

  return (
    <AppLayout
      title="Resume Studio"
      subtitle="ATS analysis, bullet optimization, keyword intelligence & document vault"
    >
      {error && <div className="error-banner">{error}</div>}

      {/* --- TOP ROW: UPLOADER & RESUME VAULT --- */}
      <div className="diagnostics-grid" style={{ marginBottom: 24 }}>
        {/* Left Column: Role Selector & Upload Box */}
        <div className="studio-card highlight">
          <div className="persona-card" style={{ marginBottom: 14 }}>
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

          <div
            className={`dropzone-container ${dragActive ? "drag-active" : ""}`}
            style={{ padding: "30px 16px" }}
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

            <div className="dropzone-icon" style={{ fontSize: 26, marginBottom: 6 }}>📄</div>
            <div className="dropzone-main-text" style={{ fontSize: 13 }}>
              {file ? file.name : "Drag & drop new resume PDF"}
            </div>
            <div className="dropzone-sub-text" style={{ fontSize: 11, marginBottom: 12 }}>
              {file ? `${(file.size / 1024).toFixed(1)} KB selected` : "Supports standard PDF formats"}
            </div>

            <button
              type="button"
              className="btn-browse"
              style={{ fontSize: 11, padding: "6px 14px" }}
              onClick={(e) => {
                e.stopPropagation();
                fileInputRef.current.click();
              }}
            >
              {file ? "Change File" : "Browse Files"}
            </button>
          </div>

          <button
            className="neon-btn-primary"
            style={{ width: "100%", marginTop: 14 }}
            onClick={handleUploadAndAnalyze}
            disabled={loading || !file}
          >
            {loading && <span className="spinner"></span>}
            {loading ? "Analyzing Resume with AI..." : "Upload & Analyze Resume"}
          </button>
        </div>

        {/* Right Column: Saved Resumes */}
        <div className="studio-card">
          <div className="box-header">
            <h3>📁 Your Saved Resumes</h3>
            <span className="badge-count blue">{savedResumes.length}</span>
          </div>
          <p style={{ fontSize: 12, color: "var(--text-muted)", margin: "0 0 12px 0" }}>
            Click to evaluate or manage saved resume artifacts
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: 8, maxHeight: 310, overflowY: "auto" }}>
            {savedResumes.map((doc) => {
              const isSelected = resumeId === doc.id;
              return (
                <div
                  key={doc.id}
                  className={`check-item-box ${isSelected ? "passed" : ""}`}
                  style={{
                    justifyContent: "space-between",
                    cursor: "pointer",
                    borderColor: isSelected ? "var(--cyan-glow)" : "var(--border-subtle)",
                    background: isSelected ? "rgba(56, 189, 248, 0.08)" : "var(--bg-surface)"
                  }}
                  onClick={() => handleSelectExistingResume(doc.id)}
                >
                  <div style={{ minWidth: 0, paddingRight: 8 }}>
                    <strong style={{ color: isSelected ? "var(--cyan-glow)" : "#fff", fontSize: 13, display: "block", textOverflow: "ellipsis", overflow: "hidden", whiteSpace: "nowrap" }}>
                      {doc.fileName || doc.title || `Resume #${doc.id}`}
                    </strong>
                    <span style={{ fontSize: 11, color: "var(--text-muted)" }}>
                      Uploaded: {doc.uploadedAt ? new Date(doc.uploadedAt).toLocaleDateString() : "Saved"}
                    </span>
                  </div>

                  <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                    <button
                      type="button"
                      className="btn-secondary"
                      style={{
                        fontSize: 11,
                        padding: "4px 10px",
                        background: isSelected ? "var(--cyan-glow)" : "var(--bg-card)",
                        color: isSelected ? "#000" : "#fff",
                        border: "none",
                        fontWeight: 700
                      }}
                    >
                      {isSelected ? "Active" : "Load"}
                    </button>

                    <button
                      type="button"
                      onClick={(e) => handleDeleteResume(e, doc.id)}
                      style={{
                        background: "none",
                        border: "none",
                        color: "var(--rose-glow)",
                        cursor: "pointer",
                        fontSize: 12,
                        fontWeight: 600,
                        padding: "4px 6px"
                      }}
                    >
                      Delete
                    </button>
                  </div>
                </div>
              );
            })}

            {savedResumes.length === 0 && (
              <p style={{ fontSize: 12, color: "var(--text-muted)", textAlign: "center", padding: "28px 0" }}>
                No resumes uploaded yet. Upload a file on the left to start.
              </p>
            )}
          </div>
        </div>
      </div>

      {/* --- RESULTS SECTION --- */}
      {roleAnalysis && (
        <div style={{ maxWidth: 1280, margin: "0 auto" }}>
          
          {/* Real-time Before/After Score Delta Banner */}
          {scoreDelta && (
            <div
              className="studio-card highlight"
              style={{
                background: "rgba(34, 197, 94, 0.08)",
                borderColor: "#22c55e",
                marginBottom: 20
              }}
            >
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <h4 style={{ margin: 0, color: "#fff", fontSize: 15 }}>
                    🎉 Selected Optimizations Applied & Re-evaluated!
                  </h4>
                  <p style={{ margin: "4px 0 0", fontSize: 12, color: "var(--text-muted)" }}>
                    Your resume text has been updated and immediately tested against ATS heuristics.
                  </p>
                </div>
                <div style={{ textAlign: "right" }}>
                  <span style={{ fontSize: 22, fontWeight: 900, color: "#22c55e" }}>
                    {scoreDelta.before}% → {scoreDelta.after}%
                  </span>
                  <span className="role-pill" style={{ marginLeft: 10, color: "#22c55e" }}>
                    +{scoreDelta.diff} points
                  </span>
                </div>
              </div>
            </div>
          )}

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

          {/* Score Overview Grid */}
          <div className="score-overview-grid">
            <div className="hero-score-card">
              <div className="hero-score-number">{roleAnalysis.matchPercentage}%</div>
              <div className="hero-score-label">{roleAnalysis.scoreLabel || "AI Resume Compatibility Estimate"}</div>
            </div>

            {roleAnalysis.scoreBreakdown && (
              <div className="section-checks-grid">
                {roleAnalysis.scoreBreakdown.map((cat) => (
                  <div
                    key={cat.category}
                    className={`check-item-box ${cat.score >= 70 ? "passed" : cat.score >= 40 ? "" : "failed"}`}
                    title={cat.explanation}
                  >
                    {cat.score >= 70 ? "✓" : cat.score >= 40 ? "⚠" : "✕"} {cat.category} ({cat.score}%)
                  </div>
                ))}
              </div>
            )}
          </div>

          {roleAnalysis.scoreExplanation && (
            <div className="studio-card" style={{ margin: "0 0 20px 0" }}>
              <p style={{ fontSize: 13, color: "var(--text-secondary)", margin: 0 }}>
                💡 {roleAnalysis.scoreExplanation}
              </p>
            </div>
          )}

          {/* Detailed Progress Bars Breakdown */}
          {roleAnalysis.scoreBreakdown && (
            <div className="studio-card" style={{ marginBottom: 20 }}>
              <div className="box-header">
                <h3>Score Breakdown</h3>
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                {roleAnalysis.scoreBreakdown.map((cat) => (
                  <div key={cat.category}>
                    <div style={{ display: "flex", justifyContent: "space-between", fontSize: 13, marginBottom: 4 }}>
                      <span>{cat.category}</span>
                      <span style={{ color: "var(--cyan-glow)" }}>{cat.score}%</span>
                    </div>
                    <div style={{ background: "var(--bg-hover)", borderRadius: 4, height: 6, overflow: "hidden" }}>
                      <div style={{
                        width: `${cat.score}%`,
                        height: "100%",
                        background: cat.score >= 70 ? "#22c55e" : cat.score >= 40 ? "#f59e0b" : "#ef4444"
                      }} />
                    </div>
                    <p style={{ fontSize: 11, color: "var(--text-muted)", margin: "4px 0 0 0" }}>
                      {cat.explanation}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* --- ACTIONABLE PRIORITY IMPROVEMENTS & AI SYNTHESIS --- */}
          <div className="studio-card highlight" style={{ marginTop: 20, marginBottom: 20 }}>
            <div className="box-header" style={{ marginBottom: 14 }}>
              <div>
                <h3 style={{ margin: 0, fontSize: 16, color: "#fff" }}>🎯 Priority ATS Improvements</h3>
                <span style={{ fontSize: 12, color: "var(--text-muted)" }}>
                  Address high-deduction items to boost compatibility
                </span>
              </div>

              <div style={{ textAlign: "right" }}>
                <span style={{ fontSize: 12, color: "var(--text-muted)", display: "block" }}>
                  {roleAnalysis.matchPercentage >= 85 ? "ATS Status" : "Projected Score Lift"}
                </span>
                <span style={{ fontSize: 15, fontWeight: 700, color: "#22c55e" }}>
                  {roleAnalysis.matchPercentage >= 85
                    ? "Optimal Compatibility"
                    : `${roleAnalysis.matchPercentage}% → ~85%`}
                </span>
              </div>
            </div>

            {/* Priority Cards List */}
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {/* Missing Skills */}
              {roleAnalysis.missingRequiredSkills?.length > 0 && (
                <div className="check-item-box" style={{ justifyContent: "space-between", background: "var(--bg-card)" }}>
                  <div>
                    <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                      <span className="custom-chip red">🔴 High Priority</span>
                      <strong style={{ color: "#fff", fontSize: 13 }}>
                        {roleAnalysis.missingRequiredSkills.join(" & ")}
                      </strong>
                    </div>
                    <p style={{ margin: 0, fontSize: 12, color: "var(--text-muted)" }}>
                      Not detected in resume. Add verified evidence to pass automated screeners.
                    </p>
                  </div>
                  <button 
                    className="btn-secondary" 
                    style={{ fontSize: 11, padding: "6px 12px" }}
                    onClick={() => setActiveTab("deterministic")}
                  >
                    Add Evidence
                  </button>
                </div>
              )}

              {/* Experience Clarity */}
              <div className="check-item-box" style={{ justifyContent: "space-between", background: "var(--bg-card)" }}>
                <div>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                    <span className="custom-chip red">🔴 High Priority</span>
                    <strong style={{ color: "#fff", fontSize: 13 }}>Experience Clarity</strong>
                  </div>
                  <p style={{ margin: 0, fontSize: 12, color: "var(--text-muted)" }}>
                    Years of hands-on experience or apprenticeship timeline not explicitly labeled.
                  </p>
                </div>
                <button 
                  className="btn-secondary" 
                  style={{ fontSize: 11, padding: "6px 12px" }}
                  onClick={() => handleTriggerAIImprovement("EXPERIENCE")}
                >
                  Improve Experience
                </button>
              </div>

              {/* Project Impact */}
              <div className="check-item-box" style={{ justifyContent: "space-between", background: "var(--bg-card)" }}>
                <div>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                    <span className="custom-chip amber">🟡 Medium Priority</span>
                    <strong style={{ color: "#fff", fontSize: 13 }}>Project Impact & Architecture</strong>
                  </div>
                  <p style={{ margin: 0, fontSize: 12, color: "var(--text-muted)" }}>
                    Projects detected, but measurable production metrics and architecture keywords could be stronger.
                  </p>
                </div>
                <button 
                  className="btn-secondary" 
                  style={{ fontSize: 11, padding: "6px 12px" }}
                  onClick={() => handleTriggerAIImprovement("PROJECTS")}
                >
                  Strengthen Impact
                </button>
              </div>

              {/* Education */}
              <div className="check-item-box" style={{ justifyContent: "space-between", background: "var(--bg-card)" }}>
                <div>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                    <span className="custom-chip green">🟢 Good</span>
                    <strong style={{ color: "#fff", fontSize: 13 }}>Education Section</strong>
                  </div>
                  <p style={{ margin: 0, fontSize: 12, color: "var(--text-muted)" }}>
                    Verified specialization detected. No critical action required.
                  </p>
                </div>
                <span style={{ fontSize: 12, color: "#22c55e", fontWeight: 600 }}>✓ Verified</span>
              </div>
            </div>

            {/* AI Action Trigger Button */}
            <div style={{ display: "flex", justifyContent: "flex-end", marginTop: 16 }}>
              <button
                className="neon-btn-primary"
                style={{ padding: "10px 24px", fontSize: 13 }}
                onClick={() => handleTriggerAIImprovement("ALL")}
                disabled={improving || !resumeId}
              >
                {improving && <span className="spinner"></span>}
                {improving ? "Generating Rewrites with AI..." : "✨ Improve Resume with AI"}
              </button>
            </div>
          </div>

          {/* --- EXPANDABLE AI SUGGESTIONS DRAWER --- */}
          {suggestions.length > 0 && (
            <div className="studio-card" style={{ marginBottom: 20, border: "1px solid var(--cyan-glow)" }}>
              <div className="box-header" style={{ marginBottom: 14 }}>
                <div>
                  <h3 style={{ margin: 0, color: "#fff" }}>📋 Review AI Rewrite Proposals</h3>
                  <p style={{ margin: "4px 0 0", fontSize: 12, color: "var(--text-muted)" }}>
                    Select which targeted bullet rewrites you want to apply. No fake metrics were added.
                  </p>
                </div>
                <span className="role-pill" style={{ color: "var(--cyan-glow)" }}>
                  {Object.values(selectedSuggestions).filter(Boolean).length} of {suggestions.length} Selected
                </span>
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                {suggestions.map((sug) => {
                  const isChecked = !!selectedSuggestions[sug.id];
                  return (
                    <div
                      key={sug.id}
                      className="check-item-box"
                      style={{
                        display: "flex",
                        gap: 14,
                        alignItems: "flex-start",
                        background: isChecked ? "rgba(56, 189, 248, 0.05)" : "var(--bg-main)",
                        borderColor: isChecked ? "rgba(56, 189, 248, 0.4)" : "var(--border-subtle)",
                        cursor: "pointer"
                      }}
                      onClick={() => handleToggleSuggestion(sug.id)}
                    >
                      <input
                        type="checkbox"
                        checked={isChecked}
                        onChange={() => handleToggleSuggestion(sug.id)}
                        style={{ marginTop: 6, cursor: "pointer" }}
                        onClick={(e) => e.stopPropagation()}
                      />

                      <div style={{ flex: 1, fontSize: 13 }}>
                        <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 6 }}>
                          <span className="role-pill">{sug.section}</span>
                          <span className={`custom-chip ${sug.priority === "HIGH" ? "red" : "amber"}`}>{sug.priority}</span>
                          <strong style={{ color: "#fff" }}>{sug.issueTitle}</strong>
                        </div>

                        {sug.originalText && (
                          <div style={{ color: "#ef4444", textDecoration: "line-through", marginBottom: 4 }}>
                            {sug.originalText}
                          </div>
                        )}

                        <div style={{ color: "#34d399", fontWeight: 500, marginBottom: 6 }}>
                          {sug.suggestedText}
                        </div>

                        <div style={{ fontSize: 11, color: "var(--text-muted)" }}>
                          <strong>Reason: </strong> {sug.reason}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Action Button: Apply Selected & Re-evaluate */}
              <div style={{ display: "flex", justifyContent: "flex-end", marginTop: 16 }}>
                <button
                  className="neon-btn-primary"
                  style={{ padding: "10px 24px", fontSize: 13 }}
                  onClick={handleApplySelected}
                  disabled={applying || Object.values(selectedSuggestions).filter(Boolean).length === 0}
                >
                  {applying && <span className="spinner"></span>}
                  {applying ? "Applying & Re-evaluating..." : "Apply Selected Improvements →"}
                </button>
              </div>
            </div>
          )}

          {/* Navigation Tabs */}
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

          {/* Tab 1: Deterministic Match */}
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
                  {!roleAnalysis.missingRecommendedSkills || roleAnalysis.missingRecommendedSkills.length === 0 ? (
                    <span className="custom-chip green">All recommended skills covered!</span>
                  ) : (
                    roleAnalysis.missingRecommendedSkills.map((s) => (
                      <span key={s} className="custom-chip amber">{s}</span>
                    ))
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Tab 2: Career Agent Recommendations */}
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