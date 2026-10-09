import "./App.css";
import { useEffect, useState } from "react";
import { BrowserRouter, Navigate, Route, Routes, useLocation } from "react-router-dom";

import Header from "./components/common/Header/Header";
import Footer from "./components/common/Footer/Footer";
import Home from "./pages/frontOffice/Home/Home";
import Catalog from "./pages/frontOffice/Catalog/Catalog";
import VehicleDetail from "./pages/frontOffice/VehicleDetail/VehicleDetail";
import About from "./pages/frontOffice/About/About";
import Contact from "./pages/frontOffice/Contact/Contact";
import FAQ from "./pages/frontOffice/FAQ/FAQ";
import QuoteRequest from "./pages/frontOffice/QuoteRequest/QuoteRequest";
import QuoteConfirmation from "./pages/frontOffice/QuoteRequest/QuoteConfirmation";
import CurrencyProvider from "./context/CurrencyProvider";
import { AuthProvider, useAuth } from "./context/AuthContext";
import AdminLayout from "./components/admin/AdminLayout";
import AdminLogin from "./pages/admin/Login";
import AdminDashboard from "./pages/admin/Dashboard";
import AdminVehicles from "./pages/admin/Vehicles";
import AdminVehicleForm from "./pages/admin/VehicleForm";
import AdminCategories from "./pages/admin/Categories";
import AdminQuoteRequests from "./pages/admin/QuoteRequests";
import AdminUsers from "./pages/admin/Users";
import AdminSettings from "./pages/admin/Settings";
import VerifyEmail from "./pages/admin/VerifyEmail";

function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: "auto" });
  }, [pathname]);
  return null;
}

function AdminRoute({ children }) {
  const { user, isAdmin, loading } = useAuth();
  if (loading) return <div className="admin-loading">Chargement de la session…</div>;
  if (!user || !isAdmin()) return <Navigate to="/admin/login" replace />;
  return children;
}

function AppRoutes({ theme, setTheme }) {
  const { pathname } = useLocation();
  const isAdminPath = pathname.startsWith("/admin");

  return (
    <>
      <ScrollToTop />
      {!isAdminPath && <Header theme={theme} setTheme={setTheme} />}
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/catalogue" element={<Catalog />} />
        <Route path="/vehicule/:id" element={<VehicleDetail />} />
        <Route path="/about" element={<About />} />
        <Route path="/contact" element={<Contact />} />
        <Route path="/faq" element={<FAQ />} />
        <Route path="/devis" element={<QuoteRequest />} />
        <Route path="/devis/confirmation" element={<QuoteConfirmation />} />
        <Route path="/verify-email" element={<VerifyEmail />} />
        <Route path="/admin/verify-email" element={<VerifyEmail />} />

        <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/admin" element={<AdminRoute><AdminLayout /></AdminRoute>}>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="vehicules" element={<AdminVehicles />} />
          <Route path="vehicules/ajouter" element={<AdminVehicleForm />} />
          <Route path="vehicules/modifier/:id" element={<AdminVehicleForm />} />
          <Route path="categories" element={<AdminCategories />} />
          <Route path="devis" element={<AdminQuoteRequests />} />
          <Route path="devis/:id" element={<AdminQuoteRequests />} />
          <Route path="users" element={<AdminUsers />} />
          <Route path="settings" element={<AdminSettings />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      {!isAdminPath && <Footer />}
    </>
  );
}

function App() {
  const [theme, setTheme] = useState(() => localStorage.getItem("gmt-theme") === "light" ? "light" : "dark");
  useEffect(() => {
    document.body.classList.toggle("light-mode", theme === "light");
    localStorage.setItem("gmt-theme", theme);
  }, [theme]);

  return (
    <BrowserRouter>
      <AuthProvider>
        <CurrencyProvider>
          <AppRoutes theme={theme} setTheme={setTheme} />
        </CurrencyProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
