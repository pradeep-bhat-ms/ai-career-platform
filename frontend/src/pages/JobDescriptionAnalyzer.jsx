import { useState } from "react";
import { submitJobDescription } from "../services/jobDescriptionService";
import "./ResumeAnalyzer.css";

function JobDescriptionAnalyzer() {
  const [jobTitle, setJobTitle] = useState("");
  const [company, setCompany] = useState("");
  const [rawText, setRawText] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async () => {
    if (!jobTitle || !rawText) return;
    setLoading(true);
    setError("");
    setResult(null);
    try {
      const res = await submitJobDescription(jobTitle, company, rawText);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to analyze job description");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="resume-page">
      <h2>AI Job Description Analyzer</h2>
      {error && <div className="error-banner">{error}</div>}

      <div className="upload-card" style={{ textAlign: "left" }}>
        <label style={{ display: "block", marginBottom: 6, fontSize: 13, color: "#94a3b8" }}>
          Job Title *
        </label>
        <input
          type="text"
          value={jobTitle}
          onChange={(e) => setJobTitle(e.target.value)}
          placeholder="e.g. Java Full Stack Developer"
          style={{
            width: "100%", padding: 10, marginBottom: 14,
            borderRadius: 8, border: "1px solid #374151",
            background: "#1f2937", color: "#e2e8f0",
          }}
        />

        <label style={{ display: "block", marginBottom: 6, fontSize: 13, color: "#94a3b8" }}>
          Company (optional)
        </label>
        <input
          type="text"
          value={company}
          onChange={(e) => setCompany(e.target.value)}
          placeholder="e.g. Example Corp"
          style={{
            width: "100%", padding: 10, marginBottom: 14,
            borderRadius: 8, border: "1px solid #374151",
            background: "#1f2937", color: "#e2e8f0",
          }}
        />

        <label style={{ display: "block", marginBottom: 6, fontSize: 13, color: "#94a3b8" }}>
          Job Description Text *
        </label>
        <textarea
          value={rawText}
          onChange={(e) => setRawText(e.target.value)}
          placeholder="Paste the full job description here..."
          rows={8}
          style={{
            width: "100%", padding: 10, marginBottom: 14,
            borderRadius: 8, border: "1px solid #374151",
            background: "#1f2937", color: "#e2e8f0", fontFamily: "inherit",
          }}
        />

        <button className="primary-btn" onClick={handleSubmit} disabled={loading || !jobTitle || !rawText}>
          {loading && <span className="spinner"></span>}
          {loading ? "Analyzing..." : "Analyze Job Description"}
        </button>
      </div>

      {result && (
        <div className="section-card">
          <h3>{result.jobTitle}</h3>
          <p style={{ color: "#cbd5e1", fontSize: 14 }}>{result.extractedData.summary}</p>

          {result.extractedData.experienceLevel && (
            <p style={{ fontSize: 14 }}>
              <strong>Experience Level:</strong> {result.extractedData.experienceLevel}
            </p>
          )}

          <h3 style={{ marginTop: 20 }}>✅ Required Skills</h3>
          <div className="skill-chip-group">
            {result.extractedData.requiredSkills.map((s) => (
              <span key={s} className="chip missing-required">{s}</span>
            ))}
          </div>

          <h3 style={{ marginTop: 20 }}>💡 Preferred Skills</h3>
          <div className="skill-chip-group">
            {result.extractedData.preferredSkills.length === 0 ? (
              <span style={{ color: "#94a3b8", fontSize: 13 }}>None specified</span>
            ) : (
              result.extractedData.preferredSkills.map((s) => (
                <span key={s} className="chip missing-recommended">{s}</span>
              ))
            )}
          </div>

          {result.extractedData.responsibilities.length > 0 && (
            <>
              <h3 style={{ marginTop: 20 }}>📋 Responsibilities</h3>
              <ul style={{ color: "#cbd5e1", fontSize: 14, paddingLeft: 20 }}>
                {result.extractedData.responsibilities.map((r, idx) => (
                  <li key={idx} style={{ marginBottom: 4 }}>{r}</li>
                ))}
              </ul>
            </>
          )}
        </div>
      )}
    </div>
  );
}

export default JobDescriptionAnalyzer;