import React, { useState, useMemo } from "react";
import AppLayout from "../components/AppLayout";
import { analyzeGitHubProfile } from "../services/githubService";
import "../ResumeAnalyzer.css";

// STEP 1: URL input + real GitHub API call, profile overview and repo list.
// STEP 5 (partial): Language Distribution + Repository Statistics, computed
// client-side from data already fetched in Step 1 — no extra API calls.
// README/license-category/deployment/CI/engineering-signals/improvement-
// report/AI-optimizer panels are still NOT implemented — later steps.
function GitHubAnalyzer() {
  const [profileUrl, setProfileUrl] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [analysis, setAnalysis] = useState(null);

  const handleAnalyze = async () => {
    setError("");
    setAnalysis(null);

    if (!profileUrl.trim()) {
      setError("Please enter a GitHub profile URL.");
      return;
    }

    setLoading(true);
    try {
      const res = await analyzeGitHubProfile(profileUrl.trim());
      setAnalysis(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to analyze GitHub profile.");
    } finally {
      setLoading(false);
    }
  };

  // Repository Statistics: total / original / forked / archived.
  // Pure aggregation of fields already present on each repo — no API cost.
  const repoStats = useMemo(() => {
    if (!analysis) return null;
    const repos = analysis.repositories;
    return {
      total: repos.length,
      original: repos.filter((r) => !r.fork).length,
      forked: repos.filter((r) => r.fork).length,
      archived: repos.filter((r) => r.archived).length,
    };
  }, [analysis]);

  // Language Distribution: percentage of repositories whose PRIMARY language
  // is X. This is NOT a byte-weighted breakdown of actual code volume (that
  // would require one extra API call per repo to GitHub's /languages
  // endpoint — too costly against the 60 req/hour unauthenticated limit for
  // profiles with many repos). Labeled honestly below rather than presented
  // as more precise than it is.
  const languageDistribution = useMemo(() => {
    if (!analysis) return [];
    const repos = analysis.repositories.filter((r) => r.language);
    const counts = {};
    repos.forEach((r) => {
      counts[r.language] = (counts[r.language] || 0) + 1;
    });
    const total = repos.length;
    return Object.entries(counts)
      .map(([language, count]) => ({
        language,
        count,
        percentage: total === 0 ? 0 : Math.round((count / total) * 100),
      }))
      .sort((a, b) => b.count - a.count);
  }, [analysis]);

  return (
    <AppLayout
      title="GitHub Analyzer"
      subtitle="Analyze your GitHub profile and projects"
    >
      {error && <div className="error-banner">{error}</div>}

      <div className="studio-card highlight" style={{ marginBottom: 24 }}>
        <label className="persona-label">GitHub Profile URL</label>
        <div style={{ display: "flex", gap: 10, marginTop: 8 }}>
          <input
            type="text"
            className="persona-select"
            style={{ flex: 1 }}
            placeholder="https://github.com/username"
            value={profileUrl}
            onChange={(e) => setProfileUrl(e.target.value)}
          />
          <button
            className="neon-btn-primary"
            onClick={handleAnalyze}
            disabled={loading}
          >
            {loading && <span className="spinner"></span>}
            {loading ? "Analyzing..." : "Analyze"}
          </button>
        </div>
      </div>

      {analysis && (
        <>
          <div className="studio-card" style={{ marginBottom: 24 }}>
            <div className="box-header">
              <h3>GitHub Profile Overview</h3>
            </div>
            <div style={{ display: "flex", gap: 16, alignItems: "center", marginTop: 12 }}>
              {analysis.profile.avatar_url && (
                <img
                  src={analysis.profile.avatar_url}
                  alt={analysis.profile.login}
                  style={{ width: 64, height: 64, borderRadius: "50%" }}
                />
              )}
              <div>
                <strong style={{ color: "#fff", fontSize: 15 }}>
                  {analysis.profile.name || analysis.profile.login}
                </strong>
                <p style={{ margin: "2px 0", fontSize: 12, color: "var(--text-muted)" }}>
                  @{analysis.profile.login} {analysis.profile.location ? `· ${analysis.profile.location}` : ""}
                </p>
                {analysis.profile.bio && (
                  <p style={{ margin: "4px 0 0", fontSize: 13, color: "var(--text-secondary)" }}>
                    {analysis.profile.bio}
                  </p>
                )}
              </div>
            </div>

            <div className="section-checks-grid" style={{ marginTop: 16 }}>
              <div className="check-item-box">Repositories: {analysis.profile.public_repos}</div>
              <div className="check-item-box">Followers: {analysis.profile.followers}</div>
              <div className="check-item-box">Following: {analysis.profile.following}</div>
            </div>
          </div>

          {/* Repository Statistics */}
          <div className="diagnostics-grid" style={{ marginBottom: 24 }}>
            <div className="studio-card">
              <div className="box-header">
                <h3>Repository Statistics</h3>
              </div>
              <div className="section-checks-grid" style={{ marginTop: 12 }}>
                <div className="check-item-box">Total: {repoStats.total}</div>
                <div className="check-item-box">Original: {repoStats.original}</div>
                <div className="check-item-box">Forked: {repoStats.forked}</div>
                <div className="check-item-box">Archived: {repoStats.archived}</div>
              </div>
              <p style={{ fontSize: 11, color: "var(--text-muted)", margin: "10px 0 0" }}>
                "Original" = not a fork of another repository. "Archived" = marked read-only by the owner.
              </p>
            </div>

            {/* Language Distribution */}
            <div className="studio-card">
              <div className="box-header">
                <h3>Language Distribution</h3>
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 12 }}>
                {languageDistribution.length === 0 && (
                  <p style={{ fontSize: 12, color: "var(--text-muted)" }}>No language data detected.</p>
                )}
                {languageDistribution.map((lang) => (
                  <div key={lang.language}>
                    <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, marginBottom: 3 }}>
                      <span>{lang.language}</span>
                      <span style={{ color: "var(--cyan-glow)" }}>{lang.percentage}% ({lang.count} repos)</span>
                    </div>
                    <div style={{ background: "var(--bg-hover)", borderRadius: 4, height: 5, overflow: "hidden" }}>
                      <div style={{ width: `${lang.percentage}%`, height: "100%", background: "var(--cyan-glow)" }} />
                    </div>
                  </div>
                ))}
              </div>
              <p style={{ fontSize: 11, color: "var(--text-muted)", margin: "10px 0 0" }}>
                Based on each repository's primary language, by repository count — not a byte-weighted code volume breakdown, and not a measure of skill level.
              </p>
            </div>
          </div>

          <div className="studio-card">
            <div className="box-header">
              <h3>Repositories</h3>
              <span className="badge-count blue">{analysis.repositories.length}</span>
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 12 }}>
              {analysis.repositories.map((repo) => (
                <div
                  key={repo.name}
                  className="check-item-box"
                  style={{ justifyContent: "space-between", background: "var(--bg-card)" }}
                >
                  <div>
                    <strong style={{ color: "#fff", fontSize: 13 }}>{repo.name}</strong>
                    <p style={{ margin: "2px 0 0", fontSize: 12, color: "var(--text-muted)" }}>
                      {repo.description || "No description"}
                    </p>
                  </div>
                  <div style={{ display: "flex", gap: 10, fontSize: 12, color: "var(--text-muted)" }}>
                    <span>⭐ {repo.stargazers_count}</span>
                    <span>{repo.language || "N/A"}</span>
                    <span>{repo.license ? repo.license.name : "No License"}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </AppLayout>
  );
}

export default GitHubAnalyzer;