import axiosInstance from "./axiosConfig";

export const registerUser = (data) => {
  return axiosInstance.post("/auth/register", data);
};

export const loginUser = (data) => {
  return axiosInstance.post("/auth/login", data);
};

export const forgotPassword = (email) => {
  return axiosInstance.post("/auth/forgot-password", { email });
};

export const resetPassword = (email, otpCode, newPassword) => {
  return axiosInstance.post("/auth/reset-password", { email, otpCode, newPassword });
};