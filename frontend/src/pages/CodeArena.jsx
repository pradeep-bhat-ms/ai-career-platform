import React, { useState, useEffect } from "react";
import AppLayout from "../components/AppLayout";
import { generateDynamicProblem, judgeCodeSolution } from "../services/codeService";
import "../ResumeAnalyzer.css";

function CodeArena() {
  const [language, setLanguage] = useState("java");
  const [difficulty, setDifficulty] = useState("Easy");
  const [topic, setTopic] = useState("Arrays & Strings");

  const [problem, setProblem] = useState(null);
  const [code, setCode] = useState("");
  const [result, setResult] = useState(null);
  
  const [generating, setGenerating] = useState(false);
  const [executing, setExecuting] = useState(false);
  const [error, setError] = useState("");

  const handleGenerateProblem = async () => {
    setGenerating(true);
    setError("");
    setResult(null);

    try {
      const res = await generateDynamicProblem(language, difficulty, topic);
      setProblem(res.data);
      setCode(res.data.starterCode || "// Write your code here");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to generate AI problem.");
    } finally {
      setGenerating(false);
    }
  };

  useEffect(() => {
    handleGenerateProblem();
  }, []);

  const handleRunAndJudge = async () => {
    if (!code.trim() || !problem) return;
    setExecuting(true);
    setError("");
    setResult(null);

    try {
      const res = await judgeCodeSolution(problem.title, problem.description, language, code);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Compiler judge failed to execute.");
    } finally {
      setExecuting(false);
    }
  };

  return (
    <AppLayout
      title="LeetCode AI Arena"
      subtitle="AI-generated coding challenges, real-time multi-language editor, and strict test-case evaluation"
    >
      {error && <div className="error-banner">{error}</div>}

      {/* TOP CONTROL BAR: DYNAMIC QUESTION GENERATOR */}
      <div className="target-role-banner" style={{ marginBottom: 16 }}>
        <div style={{ display: "flex", gap: 10, alignItems: "center", flexWrap: "wrap" }}>
          <div>
            <label style={{ fontSize: 11, color: "var(--text-muted)", display: "block", marginBottom: 2 }}>Language</label>
            <select
              className="custom-select"
              value={language}
              onChange={(e) => setLanguage(e.target.value)}
              style={{ width: 120, padding: "6px 10px" }}
            >
              <option value="java">Java</option>
              <option value="python">Python 3</option>
              <option value="javascript">JavaScript</option>
              <option value="cpp">C++</option>
              <option value="sql">SQL</option>
            </select>
          </div>

          <div>
            <label style={{ fontSize: 11, color: "var(--text-muted)", display: "block", marginBottom: 2 }}>Difficulty</label>
            <select
              className="custom-select"
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value)}
              style={{ width: 110, padding: "6px 10px" }}
            >
              <option value="Easy">Easy</option>
              <option value="Medium">Medium</option>
              <option value="Hard">Hard</option>
            </select>
          </div>

          <div>
            <label style={{ fontSize: 11, color: "var(--text-muted)", display: "block", marginBottom: 2 }}>Topic / Tag</label>
            <select
              className="custom-select"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              style={{ width: 160, padding: "6px 10px" }}
            >
              <option value="Arrays & Strings">Arrays & Strings</option>
              <option value="HashMaps & Sets">HashMaps & Sets</option>
              <option value="Linked Lists">Linked Lists</option>
              <option value="Two Pointers">Two Pointers</option>
              <option value="Dynamic Programming">Dynamic Programming</option>
              <option value="SQL Queries">SQL Queries</option>
            </select>
          </div>

          <button
            type="button"
            className="neon-btn-primary"
            style={{ marginTop: 16, padding: "8px 16px", fontSize: 12 }}
            onClick={handleGenerateProblem}
            disabled={generating}
          >
            {generating && <span className="spinner"></span>}
            {generating ? "AI Generating..." : "⚡ Generate New Problem"}
          </button>
        </div>
      </div>

      {/* MAIN LEETCODE SPLIT ARENA */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1.2fr", gap: 16, alignItems: "start" }}>
        
        {/* LEFT COLUMN: PROBLEM DESCRIPTION, EXAMPLES, & CONSTRAINTS */}
        <div className="studio-card highlight" style={{ minHeight: 640 }}>
          {problem ? (
            <div>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
                <h2 style={{ color: "#fff", margin: 0, fontSize: 18 }}>{problem.title}</h2>
                <span className={`custom-chip ${problem.difficulty === "Easy" ? "green" : problem.difficulty === "Medium" ? "amber" : "red"}`}>
                  {problem.difficulty}
                </span>
              </div>
              <span className="role-pill" style={{ marginBottom: 16, display: "inline-block" }}>{problem.category}</span>

              {/* Description Body */}
              <div style={{ color: "var(--text-secondary)", fontSize: 13, lineHeight: 1.7, marginBottom: 20, whiteSpace: "pre-line" }}>
                {problem.description}
              </div>

              {/* Example Box */}
              <div style={{ background: "#090d16", border: "1px solid var(--border-subtle)", borderRadius: 8, padding: 12, marginBottom: 16 }}>
                <div style={{ fontSize: 12, fontWeight: 700, color: "#fff", marginBottom: 6 }}>Example:</div>
                <div style={{ fontSize: 12, color: "var(--text-muted)", fontFamily: "monospace" }}>
                  <strong style={{ color: "var(--cyan-glow)" }}>Input: </strong> {problem.exampleInput}
                </div>
                <div style={{ fontSize: 12, color: "var(--text-muted)", fontFamily: "monospace", marginTop: 4 }}>
                  <strong style={{ color: "var(--emerald-glow)" }}>Output: </strong> {problem.exampleOutput}
                </div>
              </div>

              {/* Constraints */}
              {problem.constraints && (
                <div>
                  <div style={{ fontSize: 12, fontWeight: 700, color: "#fff", marginBottom: 4 }}>Constraints:</div>
                  <div style={{ color: "var(--text-muted)", fontSize: 12, whiteSpace: "pre-line", fontFamily: "monospace" }}>
                    {problem.constraints}
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div style={{ textAlign: "center", padding: "80px 20px", color: "var(--text-muted)" }}>
              Generating problem...
            </div>
          )}
        </div>

        {/* RIGHT COLUMN: CODE EDITOR & OUTPUT TERMINAL */}
        <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
          <div className="studio-card highlight" style={{ padding: 14 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
              <span style={{ fontSize: 12, color: "var(--text-muted)", fontWeight: 700, textTransform: "uppercase" }}>
                Language: <strong style={{ color: "var(--cyan-glow)" }}>{language}</strong>
              </span>
              <button
                type="button"
                className="btn-secondary"
                style={{ fontSize: 11, padding: "3px 8px" }}
                onClick={() => setCode(problem?.starterCode || "")}
              >
                Reset Code
              </button>
            </div>

            {/* Monaco-style Textarea */}
            <textarea
              className="custom-textarea"
              rows={16}
              value={code}
              onChange={(e) => setCode(e.target.value)}
              style={{
                fontFamily: "Fira Code, Consolas, Monaco, monospace",
                fontSize: 13,
                lineHeight: 1.5,
                background: "#06090e",
                color: "#38bdf8",
                border: "1px solid #1e293b",
                borderRadius: 8,
                whiteSpace: "pre",
                tabSize: 4,
              }}
              spellCheck="false"
            />

            <button
              className="neon-btn-primary"
              style={{ width: "100%", marginTop: 12, padding: "12px", fontSize: 14 }}
              onClick={handleRunAndJudge}
              disabled={executing || !code.trim()}
            >
              {executing && <span className="spinner"></span>}
              {executing ? "Compiling & Running Test Cases..." : "▶ Run & Submit Code"}
            </button>
          </div>

          {/* VERDICT CONSOLE (LEETCODE TERMINAL) */}
          {result && (
            <div
              className="studio-card"
              style={{
                background: "#080c14",
                borderColor: result.passed ? "rgba(52, 211, 153, 0.5)" : "rgba(248, 113, 113, 0.5)",
                boxShadow: result.passed ? "0 0 15px rgba(52, 211, 153, 0.15)" : "0 0 15px rgba(248, 113, 113, 0.15)"
              }}
            >
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <span
                    style={{
                      fontSize: 15,
                      fontWeight: 800,
                      color: result.passed ? "var(--emerald-glow)" : "var(--rose-glow)"
                    }}
                  >
                    {result.passed ? "✓ Accepted" : "✕ " + (result.verdict || "Wrong Answer")}
                  </span>
                </div>

                <div style={{ display: "flex", gap: 8 }}>
                  <span className="role-pill">Runtime: {result.runtime || "N/A"}</span>
                  <span className="role-pill">Memory: {result.memory || "N/A"}</span>
                </div>
              </div>

              {/* Status & Test Case Details */}
              <div style={{ fontSize: 12, color: "var(--text-secondary)", marginBottom: 6 }}>
                <strong>Status: </strong> {result.testCaseDetails}
              </div>

              {/* Red Line Error Console */}
              {result.errorOutput && (
                <div
                  style={{
                    background: "rgba(248, 113, 113, 0.08)",
                    borderLeft: "3px solid var(--rose-glow)",
                    padding: "8px 12px",
                    fontFamily: "monospace",
                    fontSize: 12,
                    color: "var(--rose-glow)",
                    margin: "8px 0",
                    whiteSpace: "pre-wrap"
                  }}
                >
                  {result.errorOutput}
                </div>
              )}

              {/* Feedback */}
              {result.strengths && (
                <div style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 6 }}>
                  💡 {result.strengths}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </AppLayout>
  );
}

export default CodeArena;