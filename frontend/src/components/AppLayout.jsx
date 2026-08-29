import React from "react";
import { NavLink, useNavigate } from "react-router-dom";
import "../ResumeAnalyzer.css";

export default function AppLayout({ children, title, subtitle }) {
  const navigate = useNavigate();
  const fullName = localStorage.getItem("fullName") || "User";

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("fullName");
    navigate("/login");
  };

  const navItems = [
    { label: "Dashboard", path: "/dashboard", icon: "📊" },
    { label: "Resume Studio", path: "/resume", icon: "📄" },
    { label: "JD Analyzer", path: "/job-description", icon: "💼" },
    { label: "Match & Compare", path: "/match", icon: "🎯" },
    { label: "Interview Agent", path: "/interview", icon: "🎙" },
    { label: "Code Arena", path: "/code-arena", icon: "💻" },
    { label: "RAG Assistant", path: "/rag-assistant", icon: "🤖" },
  ];

  return (
    <div className="app-layout">
      {/* Left Navigation Sidebar */}
      <aside className="app-sidebar">
        <div className="brand-header">
          <div className="brand-logo-icon">C</div>
          <div>
            <div className="brand-title">Career Agents</div>
            <div className="brand-subtitle">AI Career Intelligence</div>
          </div>
        </div>

        <div>
          <div className="sidebar-section-title">Career Studio</div>
          <nav className="nav-menu">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
              >
                <span>{item.icon}</span>
                <span>{item.label}</span>
              </NavLink>
            ))}
          </nav>
        </div>

        <div style={{ marginTop: "auto" }}>
          <button
            onClick={handleLogout}
            className="btn-secondary"
            style={{ width: "100%", textAlign: "left", display: "flex", gap: 8, alignItems: "center" }}
          >
            <span>🚪</span> Logout
          </button>
        </div>
      </aside>

      {/* Main Viewport */}
      <div className="app-main">
        <header className="app-topbar">
          <div className="topbar-title-group">
            <h1>{title || "Career Workspace"}</h1>
            <p>{subtitle || "ATS analysis, keyword intelligence, and role preparation"}</p>
          </div>

          <div className="topbar-actions">
            <div className="user-badge">
              <div className="user-avatar">{fullName.charAt(0).toUpperCase()}</div>
              <span>{fullName}</span>
            </div>
          </div>
        </header>

        <main className="content-viewport">
          {children}
        </main>
      </div>
    </div>
  );
}