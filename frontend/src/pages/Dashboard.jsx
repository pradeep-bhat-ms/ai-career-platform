import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";

function Dashboard() {
  const navigate = useNavigate();
  const fullName = localStorage.getItem("fullName");

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("fullName");
    navigate("/login");
  };

  return (
    <div>
      <h2>Welcome, {fullName}</h2>
      <Link to="/resume">Go to Resume Analyzer</Link>
      <button onClick={handleLogout}>Logout</button>
    </div>
  );
}

export default Dashboard;