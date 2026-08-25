import axios from "axios";

const API_BASE = "http://localhost:8080/api/auth";

export const registerUser = (data) => {
  return axios.post(`${API_BASE}/register`, data);
};

export const loginUser = (data) => {
  return axios.post(`${API_BASE}/login`, data);
};