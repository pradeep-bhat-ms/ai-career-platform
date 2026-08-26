import axiosInstance from "./axiosConfig";

export const matchResumeToJob = (resumeId, jobDescriptionId) => {
  return axiosInstance.get(`/match/${resumeId}/${jobDescriptionId}`);
};