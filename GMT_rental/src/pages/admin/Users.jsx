import { useEffect, useState } from "react";
import { adminApiService } from "../../services/admin/api";

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState("");
  useEffect(() => { adminApiService.users().then(({ data }) => setUsers(data || [])).catch(() => setError("La gestion des utilisateurs n’est pas encore disponible dans l’API Admin.")); }, []);
  return <section><div className="admin-page-head"><div><h1>Utilisateurs</h1><p>Comptes et rôles enregistrés dans GMT Rental.</p></div></div>{error && <p className="admin-error">{error}</p>}<div className="admin-card admin-table-wrap"><table className="admin-table"><thead><tr><th>Nom</th><th>E-mail</th><th>Rôle</th><th>État</th></tr></thead><tbody>{users.map((u) => <tr key={u.id}><td>{u.prenom} {u.nom}</td><td>{u.email}</td><td>{u.role}</td><td>{u.actif === false ? "Inactif" : "Actif"}</td></tr>)}{!users.length && !error && <tr><td colSpan="4" className="admin-muted">Aucun utilisateur.</td></tr>}</tbody></table></div></section>;
}
