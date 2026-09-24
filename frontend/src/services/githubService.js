import axiosInstance from "./axiosConfig";

export const analyzeGitHubProfile = (profileUrl) => {
  return axiosInstance.post("/github/analyze", { profileUrl });
};