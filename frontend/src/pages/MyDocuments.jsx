import { useState, useEffect } from "react";
import AppLayout from "../components/AppLayout";
import { getMyResumes, deleteResume } from "../services/resumeService";
import { getMyJobDescriptions, deleteJobDescription } from "../services/jobDescriptionService";
import "../ResumeAnalyzer.css";

function MyDocuments() {
  const [resumes, setResumes] = useState([]);
  const [jobDescriptions, setJobDescriptions] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setError("");
      const [resumeRes, jdRes] = await Promise.all([getMyResumes(), getMyJobDescriptions()]);
      setResumes(resumeRes.data || []);
      setJobDescriptions(jdRes.data || []);
    } catch {
      setError("Could not load your documents");
    }
  };

  const handleDeleteResume = async (id) => {
    if (!window.confirm("Delete this resume? This cannot be undone.")) return;
    try {
      await deleteResume(id);
      setResumes((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || "Failed to delete resume");
    }
  };

  const handleDeleteJd = async (id) => {
    if (!window.confirm("Delete this job description? This cannot be undone.")) return;
    try {
      await deleteJobDescription(id);
      setJobDescriptions((prev) => prev.filter((jd) => jd.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || "Failed to delete job description");
    }
  };

  return (
    <AppLayout title="Document Vault" subtitle="Manage your saved resumes and target job descriptions">
      {error && <div className="error-banner">{error}</div>}

      <div className="diagnostics-grid">
        <div className="studio-card">
          <div className="box-header">
            <h3>📄 Uploaded Resumes ({resumes.length})</h3>
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 12 }}>
            {resumes.map((r) => (
              <div key={r.id} className="check-item-box" style={{ justifyContent: "space-between" }}>
                <span>{r.fileName || `Resume #${r.id}`}</span>
                <button
                  onClick={() => handleDeleteResume(r.id)}
                  style={{ background: "none", border: "none", color: "var(--rose-glow)", cursor: "pointer", fontSize: 12 }}
                >
                  Delete
                </button>
              </div>
            ))}
            {resumes.length === 0 && (
              <span style={{ color: "var(--text-muted)", fontSize: 13 }}>No resumes uploaded yet.</span>
            )}
          </div>
        </div>

        <div className="studio-card">
          <div className="box-header">
            <h3>💼 Target Job Descriptions ({jobDescriptions.length})</h3>
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 12 }}>
            {jobDescriptions.map((jd) => (
              <div key={jd.id} className="check-item-box" style={{ justifyContent: "space-between" }}>
                <span>{jd.jobTitle} {jd.company && `(${jd.company})`}</span>
                <button
                  onClick={() => handleDeleteJd(jd.id)}
                  style={{ background: "none", border: "none", color: "var(--rose-glow)", cursor: "pointer", fontSize: 12 }}
                >
                  Delete
                </button>
              </div>
            ))}
            {jobDescriptions.length === 0 && (
              <span style={{ color: "var(--text-muted)", fontSize: 13 }}>No job descriptions saved yet.</span>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  );
}

export default MyDocuments;