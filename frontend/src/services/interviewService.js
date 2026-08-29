import axiosInstance from "./axiosConfig";

export const startInterview = (role, interviewType, difficulty, totalQuestions) => {
  return axiosInstance.post("/interview/start", { role, interviewType, difficulty, totalQuestions });
};

export const submitAnswer = (questionId, answerText) => {
  return axiosInstance.post(`/interview/${questionId}/answer`, { answerText });
};