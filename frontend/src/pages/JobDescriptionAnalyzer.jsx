import { useState } from "react";
import AppLayout from "../components/AppLayout";
import { submitJobDescription } from "../services/jobDescriptionService";
import "../ResumeAnalyzer.css";

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
    <AppLayout title="Job Description Analyzer" subtitle="Extract atomic required/preferred skills and role responsibilities">
      {error && <div className="error-banner">{error}</div>}

      <div className="studio-card highlight">
        <div className="box-header">
          <h3>📋 Submit Target Job Description</h3>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
          <div className="form-group">
            <label>Job Title *</label>
            <input
              type="text"
              className="custom-input"
              value={jobTitle}
              onChange={(e) => setJobTitle(e.target.value)}
              placeholder="e.g. Java Full Stack Developer"
            />
          </div>

          <div className="form-group">
            <label>Company (Optional)</label>
            <input
              type="text"
              className="custom-input"
              value={company}
              onChange={(e) => setCompany(e.target.value)}
              placeholder="e.g. Acme Corp"
            />
          </div>
        </div>

        <div className="form-group">
          <label>Job Description Text *</label>
          <textarea
            className="custom-textarea"
            value={rawText}
            onChange={(e) => setRawText(e.target.value)}
            placeholder="Paste the full job description text here..."
            rows={7}
          />
        </div>

        <button className="neon-btn-primary" onClick={handleSubmit} disabled={loading || !jobTitle || !rawText}>
          {loading && <span className="spinner"></span>}
          {loading ? "Extracting Atomic Skills..." : "Analyze Job Description"}
        </button>
      </div>

      {result && (
        <div className="studio-card">
          <div className="box-header">
            <h3>{result.jobTitle} {result.company && `— ${result.company}`}</h3>
          </div>
          <p style={{ color: "var(--text-secondary)", fontSize: 13, margin: "0 0 16px 0" }}>
            {result.extractedData?.summary}
          </p>

          <div className="diagnostics-grid">
            <div className="studio-card" style={{ background: "var(--bg-main)" }}>
              <div className="box-header">
                <h3>✅ Required Skills</h3>
                <span className="custom-chip red">{result.extractedData?.requiredSkills?.length || 0}</span>
              </div>
              <div className="tag-collection">
                {result.extractedData?.requiredSkills?.map((s) => (
                  <span key={s} className="custom-chip red">{s}</span>
                ))}
              </div>
            </div>

            <div className="studio-card" style={{ background: "var(--bg-main)" }}>
              <div className="box-header">
                <h3>💡 Preferred Skills</h3>
                <span className="custom-chip amber">{result.extractedData?.preferredSkills?.length || 0}</span>
              </div>
              <div className="tag-collection">
                {result.extractedData?.preferredSkills?.length === 0 ? (
                  <span style={{ color: "var(--text-muted)", fontSize: 12 }}>None specified</span>
                ) : (
                  result.extractedData?.preferredSkills?.map((s) => (
                    <span key={s} className="custom-chip amber">{s}</span>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </AppLayout>
  );
}

export default JobDescriptionAnalyzer;