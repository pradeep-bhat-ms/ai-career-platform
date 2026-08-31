import { BrowserRouter, Routes, Route } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ProtectedRoute from "./components/ProtectedRoute";
import ResumeAnalyzer from "./pages/ResumeAnalyzer";
import JobDescriptionAnalyzer from "./pages/JobDescriptionAnalyzer";
import JdMatch from "./pages/JdMatch";
import MyDocuments from "./pages/MyDocuments";
import ForgotPassword from "./pages/ForgotPassword";
import RagAssistant from "./pages/RagAssistant";
import InterviewAgent from "./pages/InterviewAgent";
import CodeArena from "./pages/CodeArena";
import SkillGapAndRoadmap from "./pages/SkillGapAndRoadmap";
import "./ResumeAnalyzer.css";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        
        {/* Make sure /resume points to ResumeAnalyzer */}
        <Route
          path="/resume"
          element={
            <ProtectedRoute>
              <ResumeAnalyzer />
            </ProtectedRoute>
          }
        />
        
        <Route
          path="/job-description"
          element={
            <ProtectedRoute>
              <JobDescriptionAnalyzer />
            </ProtectedRoute>
          }
        />
        
        <Route
          path="/match"
          element={
            <ProtectedRoute>
              <JdMatch />
            </ProtectedRoute>
          }
        />
        
        <Route
          path="/my-documents"
          element={
            <ProtectedRoute>
              <MyDocuments />
            </ProtectedRoute>
          }
        />
        
        {/* Make sure /rag-assistant points to RagAssistant */}
        <Route
          path="/rag-assistant"
          element={
            <ProtectedRoute>
              <RagAssistant />
            </ProtectedRoute>
          }
        />
        <Route
  path="/interview"
  element={
    <ProtectedRoute>
      <InterviewAgent />
    </ProtectedRoute>
  }
/>
<Route path="/code-arena" element={<ProtectedRoute><CodeArena /></ProtectedRoute>} />
<Route path="/skill-gap" element={<ProtectedRoute><SkillGapAndRoadmap /></ProtectedRoute>} />
        
      </Routes>
    </BrowserRouter>
  );
}

export default App;