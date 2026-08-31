import axiosInstance from "./axiosConfig";

export const getDashboardSummary = () => {
  return axiosInstance.get("/dashboard/summary");
};