import React, { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import axiosInstance from "../services/axiosConfig";

function ProtectedRoute({ children, allowGuest = true }) {
  const [status, setStatus] = useState("checking");

  useEffect(() => {
    let isMounted = true;

    // Check if user has explicitly initiated a guest session
    const isGuestSession = localStorage.getItem("cn_is_guest") === "true";

    axiosInstance
      .get("/auth/me")
      .then((res) => {
        if (isMounted) {
          if (res.data?.fullName) {
            localStorage.setItem("fullName", res.data.fullName);
            // Clear guest flag upon successful authentication
            localStorage.removeItem("cn_is_guest");
          }
          setStatus("authed");
        }
      })
      .catch(() => {
        if (isMounted) {
          localStorage.removeItem("fullName");
          
          // If the user deliberately launched Guest/Demo mode, allow guest access
          if (isGuestSession) {
            setStatus("guest");
          } else {
            setStatus("unauthed");
          }
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  if (status === "checking") {
    return (
      <div
        style={{
          height: "100vh",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          background: "var(--bg-main, #0b0f17)",
          color: "var(--cyan-glow, #38bdf8)",
        }}
      >
        <span className="spinner"></span>
      </div>
    );
  }

  // Redirect completely unauthenticated visitors (neither logged-in nor guest) to /login
  if (status === "unauthed") {
    return <Navigate to="/login" replace />;
  }

  // Block guest users from routes that explicitly forbid guest mode (allowGuest={false})
  if (status === "guest" && !allowGuest) {
    return <Navigate to="/login" replace />;
  }

  // Render child routes for authed users or permitted guest sessions
  return children;
}

export default ProtectedRoute;