import axios from "axios";

// One base URL for both public pages and the authenticated admin client.
export const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8081/api";

export const publicApi = axios.create({ baseURL: API_BASE_URL });
export const authenticatedApi = axios.create({ baseURL: API_BASE_URL });

authenticatedApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("adminToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const vehicleApi = {
  getAll: (params) => publicApi.get("/vehicules", { params }),
  getById: (id) => publicApi.get(`/vehicules/${id}`),
};

export const categoryApi = {
  getAll: () => publicApi.get("/categories"),
};

export const quoteRequestApi = {
  create: (data) => publicApi.post("/public/demandes", data),
};

// Keep the existing default export for the account context's legacy register call.
const API = axios.create({ baseURL: API_BASE_URL });
API.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default API;
