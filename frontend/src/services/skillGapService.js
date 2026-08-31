import axiosInstance from "./axiosConfig";

export const getSkillGap = (resumeId, targetRole) => {
  return axiosInstance.get("/skill-gap", { params: { resumeId, targetRole } });
};

export const generateLearningPlan = (resumeId, targetRole) => {
  return axiosInstance.post("/learning-plan/generate", null, { params: { resumeId, targetRole } });
};