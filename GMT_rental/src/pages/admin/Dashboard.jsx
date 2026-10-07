import { useEffect, useState } from "react";
import { CarFront, CircleDollarSign, ClipboardList, Tags } from "lucide-react";
import { Link } from "react-router-dom";
import { adminApiService } from "../../services/admin/api";
import { requestStatusBadgeClass } from "../../utils/adminStatus";

const number = (value) => Number(value || 0).toLocaleString("fr-TN");

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [requests, setRequests] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([adminApiService.dashboard(), adminApiService.quoteRequests()])
      .then(([summary, list]) => {
        setStats(summary.data || {});
        setRequests((list.data || []).slice(0, 5));
      })
      .catch(() => setError("Les données du tableau de bord ne sont pas accessibles pour le moment."));
  }, []);

  const cards = [
    [CarFront, "Véhicules", stats?.totalVehicules ?? stats?.totalVehicles],
    [Tags, "Catégories", stats?.totalCategories],
    [ClipboardList, "Demandes en attente", stats?.demandesEnAttente ?? stats?.pendingRequests],
    [CircleDollarSign, "Demandes reçues", stats?.totalDemandes ?? stats?.totalRequests],
  ];

  return <section>
    <div className="admin-page-head"><div><h1>Tableau de bord</h1><p>Vue d’ensemble de votre activité de location.</p></div></div>
    {error && <p className="admin-error">{error}</p>}
    <div className="admin-grid">{cards.map(([Icon, label, value]) => <article className="admin-card admin-stat" key={label}><span className="admin-stat-icon"><Icon size={20} /></span><div><span>{label}</span><strong>{number(value)}</strong></div></article>)}</div>
    <section className="admin-card"><div className="admin-page-head"><div><h2>Demandes récentes</h2><p>Les dernières demandes de devis reçues.</p></div><Link className="admin-button" to="/admin/devis">Toutes les demandes</Link></div>
      <div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>Client</th><th>Véhicule</th><th>Période</th><th>Montant</th><th>Statut</th></tr></thead><tbody>
        {requests.map((item) => { const status = item.statut || "NOUVELLE"; return <tr key={item.id}><td>{item.prenom} {item.nom}<br /><span className="admin-muted">{item.email}</span></td><td>{item.vehicule?.marque} {item.vehicule?.modele}</td><td>{item.dateDebut} – {item.dateFin}</td><td>{item.prixTotal ?? "—"} DT</td><td><span className={requestStatusBadgeClass(status)}>{status}</span></td></tr>; })}
        {!requests.length && <tr><td colSpan="5" className="admin-muted">Aucune demande à afficher.</td></tr>}
      </tbody></table></div>
    </section>
  </section>;
}
