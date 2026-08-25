import { useState, useEffect } from "react";
import "./ResumeAnalyzer.css";
import { uploadResume, analyzeResume, getAvailableRoles, analyzeForRole, runCareerAgent } from "../services/resumeService";

function ResumeAnalyzer() {
  const [file, setFile] = useState(null);
  const [resumeId, setResumeId] = useState(null);
  const [extractedData, setExtractedData] = useState(null);
  const [roles, setRoles] = useState([]);
  const [selectedRole, setSelectedRole] = useState("");
  const [roleAnalysis, setRoleAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState("deterministic"); // "deterministic" | "agent"
const [careerAgentData, setCareerAgentData] = useState(null);
const [agentLoading, setAgentLoading] = useState(false);

  useEffect(() => {
    getAvailableRoles()
      .then((res) => setRoles(res.data))
      .catch(() => setError("Could not load target roles"));
  }, []);



  const handleUpload = async () => {
    if (!file) return;
    setLoading(true);
    setError("");
    setRoleAnalysis(null);
    try {
      const uploadRes = await uploadResume(file);
      setResumeId(uploadRes.data.resumeId);
      const analyzeRes = await analyzeResume(uploadRes.data.resumeId);
      setExtractedData(analyzeRes.data.extractedData);
    } catch (err) {
      setError(err.response?.data?.message || "Upload or analysis failed");
    } finally {
      setLoading(false);
    }
  };

  const handleRoleAnalysis = async () => {
    if (!resumeId || !selectedRole) return;
    setLoading(true);
    setError("");
    try {
      const res = await analyzeForRole(resumeId, selectedRole);
      setRoleAnalysis(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Role analysis failed");
    } finally {
      setLoading(false);
    }
  };
  const handleCareerAgent = async () => {
  if (!resumeId || !selectedRole) return;
  setAgentLoading(true);
  setError("");
  try {
    const res = await runCareerAgent(resumeId, selectedRole);
    setCareerAgentData(res.data);
  } catch (err) {
    setError(err.response?.data?.message || "Career agent analysis failed");
  } finally {
    setAgentLoading(false);
  }
};

  return (
    <div className="resume-page">
      <h2>AI Resume Analyzer</h2>
      {error && <div className="error-banner">{error}</div>}

      {/* Upload card */}
      <div className="upload-card">
        <input
          type="file"
          accept="application/pdf"
          onChange={(e) => setFile(e.target.files[0])}
        />
        <button className="primary-btn" onClick={handleUpload} disabled={loading || !file}>
          {loading && <span className="spinner"></span>}
          {loading ? "Processing..." : "Upload & Analyze Resume"}
        </button>
      </div>

      {/* Extracted data card */}
      {extractedData && (
        <div className="section-card">
          <h3>📄 Extracted Resume Data</h3>
          <p>{extractedData.summary}</p>
          <strong>Skills</strong>
          <div className="skill-chip-group">
            {extractedData.technicalSkills.map((s) => (
              <span key={s} className="chip optional">{s}</span>
            ))}
          </div>
        </div>
      )}

      {/* Role selection card */}
      {extractedData && (
  <div className="section-card">
    <h3>🎯 Analyze for a Target Role</h3>
    <div className="role-select-row">
      <select value={selectedRole} onChange={(e) => setSelectedRole(e.target.value)}>
        <option value="">Select a role</option>
        {roles.map((role) => (
          <option key={role} value={role}>{role}</option>
        ))}
      </select>
      <button
        className="primary-btn"
        onClick={() => {
          handleRoleAnalysis();
          handleCareerAgent();
        }}
        disabled={loading || agentLoading || !selectedRole}
      >
        {(loading || agentLoading) && <span className="spinner"></span>}
        Analyze
      </button>
    </div>
  </div>
)}

{(roleAnalysis || careerAgentData) && (
  <div className="section-card" style={{ paddingBottom: 0 }}>
    <div style={{ display: "flex", gap: 8, borderBottom: "1px solid #374151", marginBottom: 20 }}>
      <button
        onClick={() => setActiveTab("deterministic")}
        style={{
          background: "none", border: "none", padding: "10px 16px", cursor: "pointer",
          color: activeTab === "deterministic" ? "#a78bfa" : "#94a3b8",
          borderBottom: activeTab === "deterministic" ? "2px solid #a78bfa" : "2px solid transparent",
          fontWeight: 600,
        }}
      >
        📊 Skill Match Score
      </button>
      <button
        onClick={() => setActiveTab("agent")}
        style={{
          background: "none", border: "none", padding: "10px 16px", cursor: "pointer",
          color: activeTab === "agent" ? "#a78bfa" : "#94a3b8",
          borderBottom: activeTab === "agent" ? "2px solid #a78bfa" : "2px solid transparent",
          fontWeight: 600,
        }}
      >
        🤖 Career Skill Agent
      </button>
    </div>
        {activeTab === "agent" && careerAgentData && (
      <div>
        <h3 style={{ marginTop: 0 }}>✅ Relevant Skills You Have</h3>
        <div className="skill-chip-group">
          {careerAgentData.alreadyHave.map((s) => <span key={s} className="chip matched">{s}</span>)}
        </div>

        <h3 style={{ marginTop: 20 }}>⚠️ Important Missing Skills</h3>
        <div className="skill-chip-group">
          {careerAgentData.importantMissingSkills.map((s) => <span key={s} className="chip missing-required">{s}</span>)}
        </div>

        <h3 style={{ marginTop: 20 }}>🎯 Recommended Next Skills</h3>
        {careerAgentData.recommendedNextSkills.map((item, idx) => (
          <div key={idx} className="suggestions-box" style={{ marginTop: 10 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <strong>{item.skill}</strong>
              <span
                className="chip"
                style={{
                  background: item.priority === "High" ? "rgba(239,68,68,0.15)" :
                              item.priority === "Medium" ? "rgba(245,158,11,0.15)" : "rgba(148,163,184,0.15)",
                  color: item.priority === "High" ? "#f87171" :
                         item.priority === "Medium" ? "#fbbf24" : "#94a3b8",
                }}
              >
                {item.priority}
              </span>
            </div>
            <p style={{ margin: "6px 0 0", fontSize: 13, color: "#cbd5e1" }}>{item.reason}</p>
          </div>
        ))}
      </div>
    )}
  </div>
)}

      {/* Results card */}
{roleAnalysis && (
  <div className="section-card">
    <h3>{roleAnalysis.targetRole}</h3>

    <div className="score-row">
      <div className="score-box">
        <div className="score-number">{roleAnalysis.matchPercentage}%</div>
        <div className="score-label">Role Match</div>
      </div>

      <div className="score-panels">
        <div className="mini-panel">
          <span className="label">✅ Skills You Have</span>
          <span className="count">{roleAnalysis.matchedSkills.length}</span>
        </div>
        <div className="mini-panel">
          <span className="label">⚠️ Missing Required</span>
          <span className="count">{roleAnalysis.missingRequiredSkills.length}</span>
        </div>
        <div className="mini-panel">
          <span className="label">💡 Recommended to Learn</span>
          <span className="count">{roleAnalysis.missingRecommendedSkills.length}</span>
        </div>
      </div>
    </div>

    <h3 style={{ marginTop: 24 }}>✅ Skills You Have</h3>
    <div className="skill-chip-group">
      {roleAnalysis.matchedSkills.length === 0 ? (
        <span style={{ color: "#94a3b8", fontSize: 13 }}>None matched yet</span>
      ) : (
        roleAnalysis.matchedSkills.map((s) => <span key={s} className="chip matched">{s}</span>)
      )}
    </div>

    <h3 style={{ marginTop: 20 }}>⚠️ Missing Required Skills</h3>
    <div className="skill-chip-group">
      {roleAnalysis.missingRequiredSkills.length === 0 ? (
        <span className="chip matched">All required skills covered!</span>
      ) : (
        roleAnalysis.missingRequiredSkills.map((s) => <span key={s} className="chip missing-required">{s}</span>)
      )}
    </div>

    <h3 style={{ marginTop: 20 }}>💡 Recommended to Learn</h3>
    <div className="skill-chip-group">
      {roleAnalysis.missingRecommendedSkills.map((s) => <span key={s} className="chip missing-recommended">{s}</span>)}
    </div>

    <h3 style={{ marginTop: 20 }}>🧩 Optional (nice to have)</h3>
    <div className="skill-chip-group">
      {roleAnalysis.optionalSkills.map((s) => <span key={s} className="chip optional">{s}</span>)}
    </div>

    <div className="suggestions-box">
      <strong>🎯 AI Suggestions</strong>
      <p style={{ margin: "6px 0 0" }}>{roleAnalysis.suggestions}</p>
    </div>
  </div>
)}
    </div>
  );
}

export default ResumeAnalyzer;