import React, { useState } from "react";
import AppLayout from "../components/AppLayout";
import { startInterview, submitAnswer } from "../services/interviewService";
import "../ResumeAnalyzer.css";

const PRESET_ROLES = [
  "Java Full Stack Developer",
  "Frontend Developer (React / Vue)",
  "Backend Developer (Spring Boot / Node.js)",
  "Full Stack Web Developer (MERN)",
  "Software Engineer",
  "DevOps Engineer",
  "Data Scientist / AI Engineer",
  "Mobile App Developer (Android / React Native)",
  "Cloud & Systems Engineer"
];

function InterviewAgent() {
  const [stage, setStage] = useState("setup"); // setup | interview | complete
  const [role, setRole] = useState(""); // Starts empty (placeholder only)
  const [interviewType, setInterviewType] = useState("Technical");
  const [difficulty, setDifficulty] = useState("Medium");
  const [totalQuestions, setTotalQuestions] = useState(5);

  const [currentQuestion, setCurrentQuestion] = useState(null);
  const [answerText, setAnswerText] = useState("");
  const [history, setHistory] = useState([]); // {question, answer, score, strengths, weaknesses}

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleStart = async (e) => {
    if (e) e.preventDefault();
    if (!role.trim()) {
      setError("Please select or type a target job role.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const res = await startInterview(role.trim(), interviewType, difficulty, totalQuestions);
      setCurrentQuestion(res.data);
      setHistory([]);
      setStage("interview");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to start interview session.");
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitAnswer = async () => {
    if (!answerText.trim() || !currentQuestion) return;
    setLoading(true);
    setError("");
    try {
      const res = await submitAnswer(currentQuestion.questionId, answerText);

      setHistory((prev) => [
        ...prev,
        {
          question: currentQuestion.questionText,
          answer: answerText,
          score: res.data.score,
          strengths: res.data.strengths,
          weaknesses: res.data.weaknesses,
        },
      ]);
      setAnswerText("");

      if (res.data.sessionComplete) {
        setStage("complete");
      } else {
        setCurrentQuestion(res.data.nextQuestion);
      }
    } catch (err) {
      setError(err.response?.data?.message || "Failed to submit answer.");
    } finally {
      setLoading(false);
    }
  };

  const handleRestart = () => {
    setStage("setup");
    setRole("");
    setCurrentQuestion(null);
    setHistory([]);
    setAnswerText("");
    setError("");
  };

  const totalScore = history.reduce((sum, h) => sum + (h.score || 0), 0);
  const maxScore = (history.length || 1) * 10;
  const scorePercentage = Math.round((totalScore / maxScore) * 100);

  return (
    <AppLayout
      title="AI Interview Agent"
      subtitle="Interactive mock interview simulator with real-time question generation and AI feedback"
    >
      {error && <div className="error-banner">{error}</div>}

      <div style={{ maxWidth: 880, margin: "0 auto" }}>
        {/* STAGE 1: SETUP FORM */}
        {stage === "setup" && (
          <div className="studio-card highlight">
            <div className="box-header" style={{ marginBottom: 16 }}>
              <span style={{ fontSize: 18 }}>🎙</span>
              <h3>Set Up Your Interview</h3>
            </div>

            {/* Target Role Dropdown / Type Combo */}
            <div className="form-group">
              <label>Target Role *</label>
              <input
                type="text"
                list="role-options"
                className="custom-input"
                placeholder="Type your custom role or select from dropdown..."
                value={role}
                onChange={(e) => setRole(e.target.value)}
                autoComplete="off"
              />
              <datalist id="role-options">
                {PRESET_ROLES.map((r, idx) => (
                  <option key={idx} value={r} />
                ))}
              </datalist>

              {/* Quick Select Preset Pills */}
              <div className="tag-collection" style={{ marginTop: 8 }}>
                <span style={{ fontSize: 11, color: "var(--text-muted)", alignSelf: "center" }}>
                  Quick picks:
                </span>
                {PRESET_ROLES.slice(0, 4).map((r, idx) => (
                  <button
                    key={idx}
                    type="button"
                    className="custom-chip blue"
                    style={{ cursor: "pointer", fontSize: 11, padding: "3px 10px" }}
                    onClick={() => setRole(r)}
                  >
                    {r}
                  </button>
                ))}
              </div>
            </div>

            <div className="diagnostics-grid" style={{ marginBottom: 0 }}>
              <div className="form-group">
                <label>Interview Type</label>
                <select
                  className="custom-select"
                  value={interviewType}
                  onChange={(e) => setInterviewType(e.target.value)}
                >
                  <option value="Technical">Technical</option>
                  <option value="HR">HR & Behavioral</option>
                  <option value="Project">Project & Architecture</option>
                </select>
              </div>

              <div className="form-group">
                <label>Difficulty</label>
                <select
                  className="custom-select"
                  value={difficulty}
                  onChange={(e) => setDifficulty(e.target.value)}
                >
                  <option value="Easy">Easy (Entry Level)</option>
                  <option value="Medium">Medium (Mid Level)</option>
                  <option value="Hard">Hard (Senior Level)</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label>Number of Questions</label>
              <select
                className="custom-select"
                value={totalQuestions}
                onChange={(e) => setTotalQuestions(Number(e.target.value))}
              >
                <option value={3}>3 Questions (Quick Drill)</option>
                <option value={5}>5 Questions (Standard Practice)</option>
                <option value={10}>10 Questions (Full Mock)</option>
              </select>
            </div>

            <button
              className="neon-btn-primary"
              style={{ width: "100%", marginTop: 12, padding: "12px 20px" }}
              onClick={handleStart}
              disabled={loading || !role.trim()}
            >
              {loading && <span className="spinner"></span>}
              {loading ? "Preparing Interview Session..." : "Start Interview"}
            </button>
          </div>
        )}

        {/* STAGE 2: ACTIVE QUESTION ARENA */}
        {stage === "interview" && currentQuestion && (
          <div className="studio-card highlight">
            <div className="target-role-banner" style={{ marginBottom: 16 }}>
              <div className="banner-left">
                <span className="role-pill">
                  Question {currentQuestion.questionNumber} of {currentQuestion.totalQuestions}
                </span>
                <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>{role}</span>
              </div>
              <span className="role-pill" style={{ color: "var(--amber-glow)", borderColor: "rgba(251, 191, 36, 0.3)" }}>
                {difficulty} • {interviewType}
              </span>
            </div>

            <div style={{ fontSize: 16, fontWeight: 700, color: "#fff", lineHeight: 1.6, margin: "16px 0 20px" }}>
              {currentQuestion.questionText}
            </div>

            <div className="form-group">
              <label>Your Answer *</label>
              <textarea
                className="custom-textarea"
                rows={7}
                placeholder="Structure your answer clearly with core concepts, architectural points, or STAR examples..."
                value={answerText}
                onChange={(e) => setAnswerText(e.target.value)}
                disabled={loading}
              />
            </div>

            <button
              className="neon-btn-primary"
              style={{ width: "100%", padding: "12px 20px" }}
              onClick={handleSubmitAnswer}
              disabled={loading || !answerText.trim()}
            >
              {loading && <span className="spinner"></span>}
              {loading ? "Evaluating..." : "Submit Answer"}
            </button>

            {/* PREVIOUS ANSWERS LIST */}
            {history.length > 0 && (
              <div style={{ marginTop: 28, borderTop: "1px solid var(--border-subtle)", paddingTop: 18 }}>
                <div className="box-header">
                  <h3>Previous Answers</h3>
                  <span className="badge-count blue">{history.length}</span>
                </div>

                {history.map((h, idx) => (
                  <div key={idx} className="interview-review-box">
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
                      <strong style={{ color: "#fff", fontSize: 13 }}>Q{idx + 1}</strong>
                      <span className={`interview-score-pill ${h.score >= 7 ? "high" : h.score >= 4 ? "medium" : "low"}`}>
                        {h.score}/10
                      </span>
                    </div>
                    <p style={{ fontSize: 12, color: "var(--text-muted)", margin: "0 0 8px 0" }}>
                      <span style={{ color: "var(--text-secondary)" }}>Your answer:</span> {h.answer}
                    </p>
                    <p style={{ fontSize: 12, color: "var(--emerald-glow)", margin: "4px 0" }}>
                      <strong>✅ Strengths:</strong> {h.strengths}
                    </p>
                    <p style={{ fontSize: 12, color: "var(--rose-glow)", margin: "4px 0" }}>
                      <strong>⚠️ Weaknesses:</strong> {h.weaknesses}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* STAGE 3: INTERVIEW COMPLETE SUMMARY */}
        {stage === "complete" && (
          <div className="studio-card highlight">
            <div style={{ textAlign: "center", padding: "10px 0 20px 0" }}>
              <div style={{ fontSize: 42, marginBottom: 8 }}>🎯</div>
              <h2 style={{ color: "#fff", margin: "0 0 6px 0" }}>Interview Complete</h2>
              <p style={{ color: "var(--text-muted)", fontSize: 13, margin: 0 }}>
                Review your final evaluation scores and feedback below.
              </p>
            </div>

            <div className="score-overview-grid" style={{ marginBottom: 20 }}>
              <div className="hero-score-card">
                <div className="hero-score-number" style={{ color: scorePercentage >= 70 ? "var(--emerald-glow)" : "var(--amber-glow)" }}>
                  {totalScore}/{maxScore}
                </div>
                <div className="hero-score-label">Total Score ({scorePercentage}%)</div>
              </div>

              <div className="section-checks-grid">
                <div className="check-item-box passed">✓ Questions Answered: {history.length}</div>
                <div className="check-item-box passed">✓ Target Role: {role}</div>
                <div className="check-item-box passed">✓ Format: {interviewType}</div>
                <div className="check-item-box passed">✓ Difficulty: {difficulty}</div>
              </div>
            </div>

            <div className="progress-bar-bg" style={{ marginBottom: 20 }}>
              <div className="progress-bar-fill" style={{ width: `${scorePercentage}%` }}></div>
            </div>

            <div className="box-header">
              <h3>Question-by-Question Review</h3>
            </div>

            {history.map((h, idx) => (
              <div key={idx} className="interview-review-box">
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
                  <strong style={{ color: "#fff", fontSize: 13 }}>Q{idx + 1}: {h.question.slice(0, 80)}...</strong>
                  <span className={`interview-score-pill ${h.score >= 7 ? "high" : h.score >= 4 ? "medium" : "low"}`}>
                    {h.score}/10
                  </span>
                </div>
                <p style={{ fontSize: 12, color: "var(--text-muted)", margin: "0 0 8px 0" }}>
                  <span style={{ color: "var(--text-secondary)" }}>Your answer:</span> {h.answer}
                </p>
                <p style={{ fontSize: 12, color: "var(--emerald-glow)", margin: "4px 0" }}>
                  <strong>✅ Strengths:</strong> {h.strengths}
                </p>
                <p style={{ fontSize: 12, color: "var(--rose-glow)", margin: "4px 0" }}>
                  <strong>⚠️ Weaknesses:</strong> {h.weaknesses}
                </p>
              </div>
            ))}

            <button
              className="neon-btn-primary"
              style={{ width: "100%", marginTop: 24, padding: "12px 20px" }}
              onClick={handleRestart}
            >
              Start New Interview
            </button>
          </div>
        )}
      </div>
    </AppLayout>
  );
}

export default InterviewAgent;