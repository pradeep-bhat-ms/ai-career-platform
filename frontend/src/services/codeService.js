import axiosInstance from "./axiosConfig";

export const generateDynamicProblem = (language, difficulty, topic) => {
  return axiosInstance.post("/code/generate", { language, difficulty, topic });
};

export const judgeCodeSolution = (problemTitle, problemDescription, language, code) => {
  return axiosInstance.post("/code/judge", {
    problemTitle,
    problemDescription,
    language,
    code,
  });
};