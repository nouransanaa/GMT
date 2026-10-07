import { authenticatedApi as adminApi } from "../api";

adminApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401
      && !error.config?.preservePageOnUnauthorized
      && window.location.pathname !== "/admin/login") {
      localStorage.removeItem("adminToken");
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.assign("/admin/login");
    }
    return Promise.reject(error);
  },
);

export const adminAuthApi = {
  login: (credentials) => adminApi.post("/auth/login", credentials),
};

export const adminApiService = {
  dashboard: () => adminApi.get("/admin/dashboard/stats"),
  vehicles: (params) => adminApi.get("/admin/vehicules", { params }),
  vehicle: (id) => adminApi.get(`/admin/vehicules/${id}`),
  createVehicle: (data) => adminApi.post("/admin/vehicules", data),
  updateVehicle: (id, data) => adminApi.put(`/admin/vehicules/${id}`, data),
  updateVehicleStatus: (id, statut) => adminApi.patch(`/admin/vehicules/${id}/statut`, null, { params: { statut } }),
  categories: () => adminApi.get("/admin/categories"),
  createCategory: (data) => adminApi.post("/admin/categories", data),
  updateCategory: (id, data) => adminApi.put(`/admin/categories/${id}`, data),
  deleteCategory: (id) => adminApi.delete(`/admin/categories/${id}`),
  quoteRequests: (params) => adminApi.get("/admin/demandes", { params }),
  quoteRequest: (id) => adminApi.get(`/admin/demandes/${id}`),
  updateQuoteRequestStatus: (id, statut) => adminApi.patch(`/admin/demandes/${id}/statut`, null, { params: { statut } }),
  users: () => adminApi.get("/admin/users"),

  createUser: (data) =>
  adminApi.post("/admin/users", data),

  updateUser: (id, data) =>
  adminApi.put(`/admin/users/${id}`, data),
  deleteUser: (id) => adminApi.delete(`/admin/users/${id}`),
  userRentals: (userId) => adminApi.get(`/admin/users/${userId}/rentals`, { preservePageOnUnauthorized: true }),
  createUserRental: (userId, data) => adminApi.post(`/admin/users/${userId}/rentals`, data, { preservePageOnUnauthorized: true }),
  settings: () => adminApi.get("/admin/settings"),
  saveSetting: (data) => adminApi.post("/admin/settings", data),
};

export default adminApi;
