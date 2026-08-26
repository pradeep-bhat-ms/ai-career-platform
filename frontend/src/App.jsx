import { BrowserRouter, Routes, Route } from "react-router-dom";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ProtectedRoute from "./components/ProtectedRoute";
import ResumeAnalyzer from "./pages/ResumeAnalyzer";
import JobDescriptionAnalyzer from "./pages/JobDescriptionAnalyzer";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
         <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route path="/resume" element={
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
        </Routes>
    </BrowserRouter>
  );
}

export default App;