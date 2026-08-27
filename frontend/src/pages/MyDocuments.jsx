import { useState, useEffect } from "react";
import { getMyResumes, deleteResume } from "../services/resumeService";
import { getMyJobDescriptions, deleteJobDescription } from "../services/jobDescriptionService";
import "./ResumeAnalyzer.css";

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
      setResumes(resumeRes.data);
      setJobDescriptions(jdRes.data);
    } catch (err) {
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

  const rowStyle = {
    display: "flex", justifyContent: "space-between", alignItems: "center",
    padding: 12, marginBottom: 8, borderRadius: 8,
    border: "1px solid #374151", background: "#1f2937", color: "#e2e8f0", fontSize: 14,
  };

  const deleteBtnStyle = { background: "none", border: "none", color: "#f87171", cursor: "pointer", fontSize: 13 };

  return (
    <div className="resume-page">
      <h2>My Documents</h2>
      <p style={{ color: "#94a3b8", fontSize: 14, marginBottom: 20 }}>
        Manage your uploaded resumes and saved job descriptions.
      </p>
      {error && <div className="error-banner">{error}</div>}

      <div className="section-card">
        <h3>📄 My Resumes ({resumes.length})</h3>
        {resumes.length === 0 ? (
          <p style={{ color: "#94a3b8", fontSize: 14 }}>No resumes uploaded yet.</p>
        ) : (
          resumes.map((r) => (
            <div key={r.id} style={rowStyle}>
              <span>{r.fileName || `Resume #${r.id}`} — {r.uploadedAt ? new Date(r.uploadedAt).toLocaleDateString() : ""}</span>
              <button onClick={() => handleDeleteResume(r.id)} style={deleteBtnStyle}>Delete</button>
            </div>
          ))
        )}
      </div>

      <div className="section-card">
        <h3>💼 My Job Descriptions ({jobDescriptions.length})</h3>
        {jobDescriptions.length === 0 ? (
          <p style={{ color: "#94a3b8", fontSize: 14 }}>No job descriptions saved yet.</p>
        ) : (
          jobDescriptions.map((jd) => (
            <div key={jd.id} style={rowStyle}>
              <span>{jd.jobTitle}{jd.company ? ` — ${jd.company}` : ""}</span>
              <button onClick={() => handleDeleteJd(jd.id)} style={deleteBtnStyle}>Delete</button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default MyDocuments;