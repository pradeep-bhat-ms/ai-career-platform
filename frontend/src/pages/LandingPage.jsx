import React from "react";
import { Link } from "react-router-dom";
import "../ResumeAnalyzer.css";

export default function LandingPage() {
  const brandName = "CareerNexus";
  const tagline = "From Skill Diagnostics to Offer Letter";

  const coreModules = [
    {
      icon: "📄",
      badge: "ATS Studio",
      title: "Atomic Resume Intelligence",
      description: "Extract verified technical competencies, calculate role match scores, and optimize for recruiter screening systems."
    },
    {
      icon: "💼",
      badge: "JD Parser",
      title: "Job Description Analyzer",
      description: "Deconstruct enterprise hiring requirements into required core skills, soft skills, and domain prerequisites."
    },
    {
      icon: "🎯",
      badge: "Gap Engine",
      title: "Match & Benchmark Compare",
      description: "Run real-time artifact comparisons to detect critical missing keywords and compatibility percentages."
    },
    {
      icon: "🗺️",
      badge: "Curriculum AI",
      title: "Skill Gap & 4-Week Roadmap",
      description: "Target weak points with structured weekly study roadmaps and verified learning milestones."
    },
    {
      icon: "🎙️",
      badge: "Interactive Mock",
      title: "AI Voice & Text Interviewer",
      description: "Practice real-time technical rounds with dynamic follow-ups, answer evaluations, and scoring telemetry."
    },
    {
      icon: "💻",
      badge: "Compiler AI",
      title: "Algorithmic Code Arena",
      description: "Solve live coding challenges in Java, Python, SQL, C++, and JavaScript with automated test runner feedback."
    },
    {
      icon: "🤖",
      badge: "PGVector AI",
      title: "RAG Document Assistant",
      description: "Chat with your custom notes, projects, and interview cheat sheets using vector-similarity semantic retrieval."
    },
    {
      icon: "📊",
      badge: "Live Telemetry",
      title: "Career Cockpit Dashboard",
      description: "Monitor end-to-end progress, average mock interview scores, artifact volumes, and skill distributions."
    }
  ];

  return (
    <div style={{
      minHeight: "100vh",
      background: "radial-gradient(circle at 50% 0%, #172554 0%, #0b0f17 55%, #05070a 100%)",
      color: "#fff",
      display: "flex",
      flexDirection: "column",
      fontFamily: "Inter, system-ui, sans-serif"
    }}>
      
      {/* Navigation Header */}
      <header style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "20px 48px",
        borderBottom: "1px solid rgba(255, 255, 255, 0.06)",
        backdropFilter: "blur(12px)",
        background: "rgba(11, 15, 23, 0.75)",
        position: "sticky",
        top: 0,
        zIndex: 100
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div className="brand-logo-icon" style={{
            width: 38,
            height: 38,
            fontSize: 18,
            background: "linear-gradient(135deg, #38bdf8, #6366f1)",
            boxShadow: "0 0 20px rgba(56, 189, 248, 0.4)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            borderRadius: 10,
            fontWeight: "bold"
          }}>
            C
          </div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 18, letterSpacing: "-0.5px", color: "#fff" }}>
              {brandName}
            </div>
            <div style={{ fontSize: 11, color: "var(--cyan-glow, #38bdf8)", fontWeight: 500 }}>
              AI Career Operating System
            </div>
          </div>
        </div>

        <div style={{ display: "flex", gap: 14, alignItems: "center" }}>
          <Link
            to="/login"
            className="btn-secondary"
            style={{
              padding: "9px 20px",
              textDecoration: "none",
              fontSize: 13,
              borderRadius: 8,
              border: "1px solid rgba(255, 255, 255, 0.12)"
            }}
          >
            Sign In
          </Link>
          <Link
            to="/register"
            className="neon-btn-primary"
            style={{
              padding: "9px 22px",
              textDecoration: "none",
              fontSize: 13,
              borderRadius: 8,
              boxShadow: "0 0 15px rgba(56, 189, 248, 0.3)"
            }}
          >
            Get Started Free →
          </Link>
        </div>
      </header>

      {/* Hero Section */}
      <main style={{
        flex: 1,
        maxWidth: 1180,
        margin: "0 auto",
        padding: "70px 24px 90px",
        textAlign: "center"
      }}>
        
        {/* Powered Badge */}
        <div style={{
          display: "inline-flex",
          alignItems: "center",
          gap: 8,
          padding: "6px 16px",
          background: "rgba(56, 189, 248, 0.08)",
          border: "1px solid rgba(56, 189, 248, 0.3)",
          borderRadius: 999,
          fontSize: 12,
          color: "var(--cyan-glow, #38bdf8)",
          marginBottom: 24,
          boxShadow: "0 0 16px rgba(56, 189, 248, 0.15)"
        }}>
          ⚡ Powered by Spring AI, PGVector & Real-time Compiler Sandbox
        </div>

        {/* Hero Title */}
        <h1 style={{
          fontSize: "clamp(2.4rem, 5vw, 3.8rem)",
          fontWeight: 900,
          lineHeight: 1.15,
          letterSpacing: "-1px",
          margin: "0 0 18px 0"
        }}>
          Accelerate Your Tech Journey <br />
          <span style={{
            background: "linear-gradient(90deg, #38bdf8 0%, #818cf8 50%, #c084fc 100%)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent"
          }}>
            {tagline}
          </span>
        </h1>

        <p style={{
          maxWidth: 780,
          margin: "0 auto 36px",
          fontSize: 16,
          color: "#94a3b8",
          lineHeight: 1.6
        }}>
          A unified, autonomous AI career intelligence platform. Parse atomic resume skills, bridge role benchmarks with 4-week roadmaps, drill mock interviews, and solve live code challenges.
        </p>

        {/* Action Button Row */}
        <div style={{
          display: "flex",
          justifyContent: "center",
          gap: 16,
          flexWrap: "wrap",
          marginBottom: 70
        }}>
          <Link
            to="/register"
            className="neon-btn-primary"
            style={{
              padding: "14px 34px",
              fontSize: 15,
              textDecoration: "none",
              borderRadius: 8,
              boxShadow: "0 0 25px rgba(56, 189, 248, 0.4)",
              fontWeight: 700
            }}
          >
            Launch Your Career Cockpit →
          </Link>
          <Link
            to="/login"
            className="btn-secondary"
            style={{
              padding: "14px 28px",
              fontSize: 15,
              textDecoration: "none",
              borderRadius: 8,
              border: "1px solid rgba(255, 255, 255, 0.15)"
            }}
          >
            Live Demo Access
          </Link>
        </div>

        {/* Telemetry Metric Ribbon */}
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
          gap: 12,
          marginBottom: 60,
          padding: "16px 20px",
          background: "rgba(15, 23, 42, 0.6)",
          border: "1px solid rgba(56, 189, 248, 0.15)",
          borderRadius: 12
        }}>
          <div>
            <div style={{ fontSize: 20, fontWeight: 800, color: "var(--cyan-glow, #38bdf8)" }}>8 Agents</div>
            <div style={{ fontSize: 11, color: "#94a3b8" }}>Integrated Architecture</div>
          </div>
          <div>
            <div style={{ fontSize: 20, fontWeight: 800, color: "var(--emerald-glow, #34d399)" }}>100% Deterministic</div>
            <div style={{ fontSize: 11, color: "#94a3b8" }}>Skill Match Scoring</div>
          </div>
          <div>
            <div style={{ fontSize: 20, fontWeight: 800, color: "#818cf8" }}>4-Week</div>
            <div style={{ fontSize: 11, color: "#94a3b8" }}>AI Dynamic Roadmaps</div>
          </div>
          <div>
            <div style={{ fontSize: 20, fontWeight: 800, color: "#fbbf24" }}>Real-time</div>
            <div style={{ fontSize: 11, color: "#94a3b8" }}>Algorithmic Code Sandbox</div>
          </div>
        </div>

        {/* Section Heading */}
        <div style={{ textAlign: "left", marginBottom: 20 }}>
          <h2 style={{ fontSize: 20, fontWeight: 700, margin: "0 0 4px 0", color: "#fff" }}>
            The Full-Stack Career Intelligence Engine
          </h2>
          <p style={{ fontSize: 13, color: "#64748b", margin: 0 }}>
            Every tool required to convert technical competence into high-tier job offers.
          </p>
        </div>

        {/* 8-Card Modular Grid */}
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
          gap: 16,
          textAlign: "left"
        }}>
          {coreModules.map((item, idx) => (
            <div
              key={idx}
              className="studio-card"
              style={{
                background: "rgba(15, 23, 42, 0.65)",
                borderColor: "rgba(255, 255, 255, 0.07)",
                transition: "transform 0.2s ease, border-color 0.2s ease",
                cursor: "pointer"
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = "translateY(-3px)";
                e.currentTarget.style.borderColor = "rgba(56, 189, 248, 0.35)";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = "translateY(0)";
                e.currentTarget.style.borderColor = "rgba(255, 255, 255, 0.07)";
              }}
            >
              <div style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: 12
              }}>
                <span style={{ fontSize: 24 }}>{item.icon}</span>
                <span className="role-pill" style={{
                  background: "rgba(56, 189, 248, 0.12)",
                  color: "var(--cyan-glow, #38bdf8)",
                  borderColor: "rgba(56, 189, 248, 0.25)",
                  fontSize: 10
                }}>
                  {item.badge}
                </span>
              </div>
              <strong style={{ color: "#fff", fontSize: 14, display: "block", marginBottom: 6 }}>
                {item.title}
              </strong>
              <p style={{ color: "#94a3b8", fontSize: 12, lineHeight: 1.5, margin: 0 }}>
                {item.description}
              </p>
            </div>
          ))}
        </div>

      </main>

      {/* Footer */}
      <footer style={{
        borderTop: "1px solid rgba(255, 255, 255, 0.06)",
        padding: "24px 48px",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        fontSize: 12,
        color: "#64748b",
        background: "#080c13"
      }}>
        <div>
          © {new Date().getFullYear()} <strong style={{ color: "#cbd5e1" }}>{brandName}</strong>. All rights reserved.
        </div>
        <div style={{ display: "flex", gap: 16 }}>
          <span>Spring AI</span>
          <span>•</span>
          <span>PostgreSQL PGVector</span>
          <span>•</span>
          <span>React 18</span>
        </div>
      </footer>

    </div>
  );
}