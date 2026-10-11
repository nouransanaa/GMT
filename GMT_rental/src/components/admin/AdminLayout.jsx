import { CarFront, ChartNoAxesCombined, FileText, FolderKanban, LogOut, Settings, Users } from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import NotificationBell from "./NotificationBell";
import "./AdminLayout.css";

const links = [
  { to: "/admin/dashboard", label: "Tableau de bord", icon: ChartNoAxesCombined },
  { to: "/admin/vehicules", label: "Véhicules", icon: CarFront },
  { to: "/admin/categories", label: "Catégories", icon: FolderKanban },
  { to: "/admin/devis", label: "Demandes de devis", icon: FileText },
  { to: "/admin/users", label: "Utilisateurs", icon: Users },
  { to: "/admin/settings", label: "Paramètres", icon: Settings },
];

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const signOut = () => { logout(); navigate("/admin/login", { replace: true }); };

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-brand"><span className="admin-brand-mark">GMT</span><span>Gestion Rental<small>ESPACE ADMIN</small></span></div>
        <nav className="admin-nav" aria-label="Navigation administration">
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} className={({ isActive }) => `admin-nav-link${isActive ? " active" : ""}`}>
              <Icon size={18} /><span>{label}</span>
            </NavLink>
          ))}
        </nav>
        <button type="button" className="admin-logout" onClick={signOut}><LogOut size={17} /> Déconnexion</button>
      </aside>
      <div className="admin-main">
        <header className="admin-topbar"><span>Administration</span><div className="admin-topbar-actions"><NotificationBell /><div className="admin-user"><span className="admin-avatar">{(user?.prenom || user?.email || "A").slice(0, 1).toUpperCase()}</span><span>{user?.prenom || user?.email || "Administrateur"}</span></div></div></header>
        <main className="admin-content"><Outlet /></main>
      </div>
    </div>
  );
}
