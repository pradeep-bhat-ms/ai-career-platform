import { useState, useEffect } from "react";
import AppLayout from "../components/AppLayout";
import { getMyResumes } from "../services/resumeService";
import { getMyJobDescriptions } from "../services/jobDescriptionService";
import { matchResumeToJob } from "../services/matchService";
import "../ResumeAnalyzer.css";

function JdMatch() {
  const [resumes, setResumes] = useState([]);
  const [jobDescriptions, setJobDescriptions] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState("");
  const [selectedJdId, setSelectedJdId] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([getMyResumes(), getMyJobDescriptions()])
      .then(([resumeRes, jdRes]) => {
        setResumes(resumeRes.data || []);
        setJobDescriptions(jdRes.data || []);
      })
      .catch(() => setError("Could not load resumes or job descriptions"));
  }, []);

  const handleMatch = async () => {
    if (!selectedResumeId || !selectedJdId) return;
    setLoading(true);
    setError("");
    setResult(null);
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
    <AppLayout title="Resume ↔ JD Matching Engine" subtitle="Calculate skill match scores and missing requirement gaps">
      {error && <div className="error-banner">{error}</div>}

      <div className="studio-card highlight">
        <div className="box-header">
          <h3>🎯 Select Artifacts to Compare</h3>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
          <div className="form-group">
            <label>Select Resume</label>
            <select
              className="custom-select"
              value={selectedResumeId}
              onChange={(e) => { setSelectedResumeId(e.target.value); setResult(null); }}
            >
              <option value="">Choose a resume</option>
              {resumes.map((r) => (
                <option key={r.id} value={r.id}>{r.fileName || `Resume #${r.id}`}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Select Job Description</label>
            <select
              className="custom-select"
              value={selectedJdId}
              onChange={(e) => { setSelectedJdId(e.target.value); setResult(null); }}
            >
              <option value="">Choose a job description</option>
              {jobDescriptions.map((jd) => (
                <option key={jd.id} value={jd.id}>{jd.jobTitle} {jd.company ? `(${jd.company})` : ""}</option>
              ))}
            </select>
          </div>
        </div>

        <button className="neon-btn-primary" onClick={handleMatch} disabled={loading || !selectedResumeId || !selectedJdId}>
          {loading && <span className="spinner"></span>}
          {loading ? "Comparing Skills..." : "Run Match Engine"}
        </button>
      </div>

      {result && (
        <div className="studio-card">
          <div className="target-role-banner" style={{ marginBottom: 14 }}>
            <div className="banner-left">
              <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>Target Role:</span>
              <strong style={{ color: "#fff" }}>{result.jobTitle}</strong>
            </div>
            <div className="role-pill" style={{ fontSize: 14 }}>{result.matchPercentage}% Compatible</div>
          </div>

          <div className="progress-bar-bg">
            <div className="progress-bar-fill" style={{ width: `${result.matchPercentage}%` }}></div>
          </div>

          <div className="diagnostics-grid">
            <div className="studio-card" style={{ background: "var(--bg-main)" }}>
              <div className="box-header">
                <h3>✅ Matched Required Skills</h3>
              </div>
              <div className="tag-collection">
                {result.matchedRequiredSkills?.length > 0 ? (
                  result.matchedRequiredSkills.map((s) => <span key={s} className="custom-chip green">{s}</span>)
                ) : (
                  <span style={{ color: "var(--text-muted)", fontSize: 12 }}>None</span>
                )}
              </div>
            </div>

            <div className="studio-card" style={{ background: "var(--bg-main)" }}>
              <div className="box-header">
                <h3>⚠️ Missing Required Skills</h3>
              </div>
              <div className="tag-collection">
                {result.missingRequiredSkills?.length === 0 ? (
                  <span className="custom-chip green">All required skills covered!</span>
                ) : (
                  result.missingRequiredSkills?.map((s) => <span key={s} className="custom-chip red">{s}</span>)
                )}
              </div>
            </div>
          </div>

          <div className="studio-card" style={{ background: "var(--bg-main)", borderColor: "rgba(56, 189, 248, 0.2)" }}>
            <div className="box-header">
              <h3>💡 AI Matching Recommendations</h3>
            </div>
            <p style={{ color: "var(--text-secondary)", fontSize: 13, lineHeight: 1.6, margin: 0, whiteSpace: "pre-line" }}>
              {result.suggestions}
            </p>
          </div>
        </div>
      )}
    </AppLayout>
  );
}

export default JdMatch;