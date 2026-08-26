import axiosInstance from "./axiosConfig";

export const submitJobDescription = (jobTitle, company, rawText) => {
  return axiosInstance.post("/job-description/submit", {
    jobTitle,
    company,
    rawText,
  });
};

export const getMyJobDescriptions = () => axiosInstance.get("/job-description/my-job-descriptions");