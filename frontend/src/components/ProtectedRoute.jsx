import React, { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import axiosInstance from "../services/axiosConfig";

function ProtectedRoute({ children }) {
  const [status, setStatus] = useState("checking");

  useEffect(() => {
    let isMounted = true;
    axiosInstance
      .get("/auth/me")
      .then((res) => {
        if (isMounted) {
          if (res.data?.fullName) {
            localStorage.setItem("fullName", res.data.fullName);
          }
          setStatus("authed");
        }
      })
      .catch(() => {
        if (isMounted) setStatus("guest");
      });
    return () => {
      isMounted = false;
    };
  }, []);

  if (status === "checking") {
    return (
      <div style={{
        height: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "var(--bg-main, #0b0f17)",
        color: "var(--cyan-glow, #38bdf8)"
      }}>
        <span className="spinner"></span>
      </div>
    );
  }

  if (status === "guest") {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;