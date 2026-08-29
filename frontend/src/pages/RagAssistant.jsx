import React, { useState, useEffect } from "react";
import AppLayout from "../components/AppLayout";
import {
  uploadDocument,
  getMyDocuments,
  deleteDocument,
  queryRag
} from "../services/documentService"; // Adjust path if your service file is named differently (e.g. documentService.js)
import "../ResumeAnalyzer.css";

function RagAssistant() {
  const [file, setFile] = useState(null);
  const [title, setTitle] = useState("");
  const [category, setCategory] = useState("");
  const [documents, setDocuments] = useState([]);
  const [uploading, setUploading] = useState(false);

  const [question, setQuestion] = useState("");
  const [filterCategory, setFilterCategory] = useState("");
  const [answerData, setAnswerData] = useState(null);
  const [asking, setAsking] = useState(false);
  const [error, setError] = useState("");

  const loadDocuments = async () => {
    try {
      const res = await getMyDocuments();
      setDocuments(res.data || []);
    } catch (err) {
      console.error("Failed to load documents", err);
    }
  };

  useEffect(() => {
    loadDocuments();
  }, []);

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file || !title) {
      setError("Please select a file and enter a document title.");
      return;
    }

    setUploading(true);
    setError("");

    try {
      await uploadDocument(file, title, category);
      setTitle("");
      setCategory("");
      setFile(null);
      const fileInput = document.getElementById("rag-file-input");
      if (fileInput) fileInput.value = "";
      loadDocuments();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to process and embed document.");
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this document and its embeddings?")) return;
    try {
      await deleteDocument(id);
      setDocuments((prev) => prev.filter((d) => d.id !== id));
    } catch (err) {
      setError("Failed to delete document.");
    }
  };

  const handleAsk = async (promptOverride) => {
    const q = promptOverride || question;
    if (!q.trim()) return;

    setAsking(true);
    setError("");
    setAnswerData(null);

    try {
      const res = await queryRag(q, filterCategory.trim() || null);
      const data = res.data;

      setAnswerData({
        answer: data.answer || data.response || (typeof data === "string" ? data : JSON.stringify(data)),
        sources: data.sources || data.sourceDocuments || data.citations || []
      });
    } catch (err) {
      setError(err.response?.data?.message || "Failed to retrieve answer from vector knowledge base.");
    } finally {
      setAsking(false);
    }
  };

  return (
    <AppLayout
      title="RAG Interview Prep Assistant"
      subtitle="Context-aware interview preparation grounded in your uploaded documents & vector embeddings"
    >
      {error && <div className="error-banner">{error}</div>}

      <div className="diagnostics-grid">
        {/* Left Side: Upload & Document Vault */}
        <div>
          <div className="studio-card highlight">
            <div className="box-header">
              <h3>📄 Upload Prep Document</h3>
            </div>
            <p style={{ fontSize: 12, color: "var(--text-muted)", margin: "0 0 14px 0" }}>
              Upload technical notes, cheat sheets, or interview guides (PDF, TXT)
            </p>

            <form onSubmit={handleUpload}>
              <div className="form-group">
                <label>Document Title *</label>
                <input
                  type="text"
                  className="custom-input"
                  placeholder="e.g. HTML & CSS Interview Guide"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label>Category (Optional)</label>
                <input
                  type="text"
                  className="custom-input"
                  placeholder="e.g. HTML, Java, Spring Boot"
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>Select File (PDF, TXT) *</label>
                <input
                  id="rag-file-input"
                  type="file"
                  className="custom-input"
                  accept=".pdf,.txt"
                  onChange={(e) => setFile(e.target.files[0])}
                  required
                />
              </div>

              <button
                type="submit"
                className="neon-btn-primary"
                style={{ width: "100%", marginTop: 8 }}
                disabled={uploading || !file || !title}
              >
                {uploading && <span className="spinner"></span>}
                {uploading ? "Chunking & Storing Embeddings..." : "Upload & Process"}
              </button>
            </form>
          </div>

          {/* Indexed Documents Card */}
          <div className="studio-card">
            <div className="box-header">
              <h3>📚 Your Documents</h3>
              <span className="badge-count blue">{documents.length}</span>
            </div>

            <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 12 }}>
              {documents.map((doc) => (
                <div key={doc.id} className="check-item-box" style={{ justifyContent: "space-between" }}>
                  <div>
                    <div style={{ fontWeight: 600, color: "#fff", fontSize: 13 }}>
                      {doc.title || doc.fileName}
                    </div>
                    <div style={{ display: "flex", gap: 8, alignItems: "center", marginTop: 2 }}>
                      <span style={{ fontSize: 11, color: "var(--text-muted)" }}>
                        {doc.category || "General"}
                      </span>
                      <span
                        className="custom-chip"
                        style={{
                          fontSize: 10,
                          padding: "2px 8px",
                          background: doc.status === "READY" ? "rgba(52, 211, 153, 0.15)" : "rgba(239, 68, 68, 0.15)",
                          color: doc.status === "READY" ? "var(--emerald-glow)" : "var(--rose-glow)",
                          borderColor: doc.status === "READY" ? "rgba(52, 211, 153, 0.3)" : "rgba(239, 68, 68, 0.3)"
                        }}
                      >
                        {doc.status || "READY"}
                      </span>
                    </div>
                  </div>

                  <button
                    onClick={() => handleDelete(doc.id)}
                    style={{
                      background: "none",
                      border: "none",
                      color: "var(--rose-glow)",
                      cursor: "pointer",
                      fontSize: 12,
                      fontWeight: 600
                    }}
                  >
                    Delete
                  </button>
                </div>
              ))}

              {documents.length === 0 && (
                <p style={{ fontSize: 12, color: "var(--text-muted)", textAlign: "center", padding: "16px 0" }}>
                  No documents indexed yet. Upload a prep document to start similarity querying.
                </p>
              )}
            </div>
          </div>
        </div>

        {/* Right Side: Question & Answer Pane */}
        <div>
          <div className="studio-card highlight" style={{ minHeight: "100%" }}>
            <div className="box-header">
              <h3>💬 Ask a Question</h3>
            </div>
            <p style={{ fontSize: 12, color: "var(--text-muted)", margin: "0 0 14px 0" }}>
              Ask anything from your uploaded documents (semantic search & LLM synthesis).
            </p>

            <div className="form-group">
              <label>Filter by Category (Optional)</label>
              <input
                type="text"
                className="custom-input"
                placeholder="e.g. HTML (leave blank to search all)"
                value={filterCategory}
                onChange={(e) => setFilterCategory(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label>Your Question *</label>
              <textarea
                className="custom-textarea"
                rows={4}
                placeholder="e.g. What is HTML? or What are the key tags used in HTML?"
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
              />
            </div>

            <div className="tag-collection" style={{ marginBottom: 16 }}>
              <button
                type="button"
                className="custom-chip blue"
                style={{ cursor: "pointer", background: "rgba(56, 189, 248, 0.12)" }}
                onClick={() => {
                  const q = "What are the core fundamentals explained in this document?";
                  setQuestion(q);
                  handleAsk(q);
                }}
              >
                💡 Core Fundamentals
              </button>

              <button
                type="button"
                className="custom-chip amber"
                style={{ cursor: "pointer", background: "rgba(251, 191, 36, 0.12)" }}
                onClick={() => {
                  const q = "Give me 5 hard interview questions based on these notes.";
                  setQuestion(q);
                  handleAsk(q);
                }}
              >
                🔥 Tough Interview Questions
              </button>
            </div>

            <button
              className="neon-btn-primary"
              style={{ width: "100%" }}
              onClick={() => handleAsk()}
              disabled={asking || !question.trim()}
            >
              {asking && <span className="spinner"></span>}
              {asking ? "Searching Vector Store & Generating Answer..." : "Ask Question"}
            </button>

            {answerData && (
              <div
                className="studio-card"
                style={{
                  marginTop: 20,
                  background: "var(--bg-main)",
                  borderColor: "rgba(56, 189, 248, 0.3)"
                }}
              >
                <div className="box-header">
                  <span style={{ fontSize: 16 }}>🤖</span>
                  <h3>Answer</h3>
                </div>

                <div
                  style={{
                    color: "var(--text-primary)",
                    fontSize: 13,
                    lineHeight: 1.7,
                    whiteSpace: "pre-wrap",
                    marginTop: 8
                  }}
                >
                  {answerData.answer}
                </div>

                {answerData.sources && answerData.sources.length > 0 && (
                  <div style={{ marginTop: 18, borderTop: "1px solid var(--border-subtle)", paddingTop: 12 }}>
                    <div style={{ fontSize: 12, fontWeight: 700, color: "var(--text-muted)", marginBottom: 8, display: "flex", alignItems: "center", gap: 6 }}>
                      <span>📎</span> Sources
                    </div>
                    <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                      {answerData.sources.map((src, idx) => (
                        <div
                          key={idx}
                          style={{
                            fontSize: 12,
                            color: "var(--text-secondary)",
                            background: "rgba(255, 255, 255, 0.02)",
                            padding: "6px 10px",
                            borderRadius: 6,
                            border: "1px solid var(--border-subtle)"
                          }}
                        >
                          <strong style={{ color: "var(--cyan-glow)" }}>
                            {src.title || src.documentTitle || "Source"}:
                          </strong>{" "}
                          {src.content || src.textSnippet || (typeof src === "string" ? src : JSON.stringify(src))}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  );
}

export default RagAssistant;