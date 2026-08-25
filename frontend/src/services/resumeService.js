import axiosInstance from "./axiosConfig";

export const uploadResume = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return axiosInstance.post("/resume/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const analyzeResume = (resumeId) => {
  return axiosInstance.post(`/resume/${resumeId}/analyze`);
};

export const getAvailableRoles = () => {
  return axiosInstance.get("/resume/available-roles");
};

export const analyzeForRole = (resumeId, targetRole) => {
  return axiosInstance.post(
    `/resume/${resumeId}/analyze-for-role`,
    null,
    { params: { targetRole } }
  );
};