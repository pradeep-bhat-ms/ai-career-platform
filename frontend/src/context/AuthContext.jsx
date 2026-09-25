import React, { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isGuest, setIsGuest] = useState(() => {
    return localStorage.getItem("cn_is_guest") === "true";
  });
  const [guestUsageCount, setGuestUsageCount] = useState(() => {
    return parseInt(localStorage.getItem("cn_demo_count") || "0", 10);
  });
  const [showLimitModal, setShowLimitModal] = useState(false);

  const MAX_DEMO_ACTIONS = 4;

  const startGuestSession = () => {
    localStorage.setItem("cn_is_guest", "true");
    setIsGuest(true);
    // Reset counter on fresh demo entry if expired
    if (!localStorage.getItem("cn_demo_count")) {
      localStorage.setItem("cn_demo_count", "0");
      setGuestUsageCount(0);
    }
  };

  const incrementGuestUsage = () => {
    if (!isGuest) return true; // Authenticated users have no limit

    if (guestUsageCount >= MAX_DEMO_ACTIONS) {
      setShowLimitModal(true);
      return false;
    }

    const newCount = guestUsageCount + 1;
    setGuestUsageCount(newCount);
    localStorage.setItem("cn_demo_count", newCount.toString());

    if (newCount >= MAX_DEMO_ACTIONS) {
      setShowLimitModal(true);
    }
    return true;
  };

  const logout = () => {
    localStorage.removeItem("cn_is_guest");
    localStorage.removeItem("cn_demo_count");
    setUser(null);
    setIsGuest(false);
    setGuestUsageCount(0);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        setUser,
        isGuest,
        startGuestSession,
        guestUsageCount,
        incrementGuestUsage,
        MAX_DEMO_ACTIONS,
        showLimitModal,
        setShowLimitModal,
        logout
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);