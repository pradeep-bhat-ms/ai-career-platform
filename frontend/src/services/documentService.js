import axiosInstance from "./axiosConfig";

export const uploadDocument = (file, title, category) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("title", title);
  if (category) formData.append("category", category);
  return axiosInstance.post("/documents/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const getMyDocuments = () => axiosInstance.get("/documents/my-documents");

export const deleteDocument = (documentId) => axiosInstance.delete(`/documents/${documentId}`);

export const queryRag = (question, category) => {
  return axiosInstance.post("/rag/query", { question, category });
};