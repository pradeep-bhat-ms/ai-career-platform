import { useState, useEffect } from "react";
import { getMyResumes } from "../services/resumeService";
import { getMyJobDescriptions } from "../services/jobDescriptionService";
import { matchResumeToJob } from "../services/matchService";
import "./ResumeAnalyzer.css";

function JdMatch() {
  const [resumes, setResumes] = useState([]);
  const [jobDescriptions, setJobDescriptions] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState("");
  const [selectedJdId, setSelectedJdId] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    getMyResumes().then((res) => setResumes(res.data)).catch(() => setError("Could not load resumes"));
    getMyJobDescriptions().then((res) => setJobDescriptions(res.data)).catch(() => setError("Could not load job descriptions"));
  }, []);

  const handleMatch = async () => {
    if (!selectedResumeId || !selectedJdId) return;
    setLoading(true);
    setError("");
    try {
      const res = await matchResumeToJob(selectedResumeId, selectedJdId);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Matching failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="resume-page">
      <h2>Resume ↔ Job Description Match</h2>
      {error && <div className="error-banner">{error}</div>}

      <div className="upload-card" style={{ textAlign: "left" }}>
        <label style={{ display: "block", marginBottom: 6, fontSize: 13, color: "#94a3b8" }}>
          Select Resume
        </label>
        <select
          value={selectedResumeId}
          onChange={(e) => setSelectedResumeId(e.target.value)}
          style={{ width: "100%", padding: 10, marginBottom: 14, borderRadius: 8, border: "1px solid #374151", background: "#1f2937", color: "#e2e8f0" }}
        >
          <option value="">Choose a resume</option>
          {resumes.map((r) => (
<option key={r.id} value={r.id}>
  {r.fileName || `Resume #${r.id}`} — {new Date(r.uploadedAt).toLocaleDateString()}
</option>          ))}
        </select> 

        <label style={{ display: "block", marginBottom: 6, fontSize: 13, color: "#94a3b8" }}>
          Select Job Description
        </label>
        <select
          value={selectedJdId}
          onChange={(e) => setSelectedJdId(e.target.value)}
          style={{ width: "100%", padding: 10, marginBottom: 14, borderRadius: 8, border: "1px solid #374151", background: "#1f2937", color: "#e2e8f0" }}
        >
          <option value="">Choose a job description</option>
          {jobDescriptions.map((jd) => (
            <option key={jd.id} value={jd.id}>{jd.jobTitle}{jd.company ? ` — ${jd.company}` : ""}</option>
          ))}
        </select>

        <button className="primary-btn" onClick={handleMatch} disabled={loading || !selectedResumeId || !selectedJdId}>
          {loading && <span className="spinner"></span>}
          Compare
        </button>
      </div>

      {result && (
        <div className="section-card">
          <div className="match-header">
            <h3>{result.jobTitle}</h3>
            <span className="match-score">{result.matchPercentage}%</span>
          </div>
          <div className="progress-bar-bg">
            <div className="progress-bar-fill" style={{ width: `${result.matchPercentage}%` }}></div>
          </div>

          <h3 style={{ marginTop: 20 }}>✅ Matched Required Skills</h3>
          <div className="skill-chip-group">
            {result.matchedRequiredSkills.map((s) => <span key={s} className="chip matched">{s}</span>)}
          </div>

          <h3 style={{ marginTop: 20 }}>⚠️ Missing Required Skills</h3>
          <div className="skill-chip-group">
            {result.missingRequiredSkills.length === 0 ? (
              <span className="chip matched">All required skills covered!</span>
            ) : (
              result.missingRequiredSkills.map((s) => <span key={s} className="chip missing-required">{s}</span>)
            )}
          </div>

          <h3 style={{ marginTop: 20 }}>💡 Matched Preferred Skills</h3>
          <div className="skill-chip-group">
            {result.matchedPreferredSkills.length === 0 ? (
              <span style={{ color: "#94a3b8", fontSize: 13 }}>None yet</span>
            ) : (
              result.matchedPreferredSkills.map((s) => <span key={s} className="chip matched">{s}</span>)
            )}
          </div>

          <h3 style={{ marginTop: 20 }}>🧩 Missing Preferred Skills</h3>
          <div className="skill-chip-group">
            {result.missingPreferredSkills.map((s) => <span key={s} className="chip missing-recommended">{s}</span>)}
          </div>

          <div className="suggestions-box">
            <strong>🎯 AI Suggestions</strong>
            <p style={{ margin: "6px 0 0" }}>{result.suggestions}</p>
          </div>
        </div>
      )}
    </div>
  );
}

export default JdMatch;