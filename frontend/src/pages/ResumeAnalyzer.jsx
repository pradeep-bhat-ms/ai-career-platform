import { useState, useEffect } from "react";
import {
  uploadResume,
  analyzeResume,
  getAvailableRoles,
  analyzeForRole,
} from "../services/resumeService";
import "./ResumeAnalyzer.css";

function ResumeAnalyzer() {
  const [file, setFile] = useState(null);
  const [resumeId, setResumeId] = useState(null);
  const [extractedData, setExtractedData] = useState(null);
  const [roles, setRoles] = useState([]);
  const [selectedRole, setSelectedRole] = useState("");
  const [roleAnalysis, setRoleAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

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
            <button className="primary-btn" onClick={handleRoleAnalysis} disabled={loading || !selectedRole}>
              {loading && <span className="spinner"></span>}
              Analyze
            </button>
          </div>
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