import { useCallback, useEffect, useMemo, useState } from "react";
import { adminApiService } from "../../services/admin/api";
import UserModal from "./UserModal";
import RentalHistoryModal from "./RentalHistoryModal";

function getRequestErrorMessage(error, fallback) {
  const message = error.response?.data?.message;

  switch (error.response?.status) {
    case 400:
      return message || "Vérifiez les informations saisies.";
    case 401:
      return "La session administrateur a expiré. Reconnectez-vous.";
    case 403:
      return "Votre compte n'a pas les droits nécessaires pour cette action.";
    case 404:
      return message || "Cet utilisateur n'existe plus.";
    case 409:
      return message || "Cette adresse e-mail est déjà utilisée.";
    case 500:
      return "Une erreur serveur est survenue. Réessayez plus tard.";
    default:
      return message || fallback;
  }
}

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [search, setSearch] = useState("");
  const [error, setError] = useState("");

  // Modal
  const [modalOpen, setModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [historyUser, setHistoryUser] = useState(null);
  const [modalRole, setModalRole] = useState("USER");
  const [saving, setSaving] = useState(false);
  const [deletingUserId, setDeletingUserId] = useState(null);

  const loadUsers = useCallback(async () => {
    try {
      const { data } = await adminApiService.users();

      setUsers(data || []);
      setError("");
    } catch (err) {
      console.error(err);
      setError(getRequestErrorMessage(err, "Impossible de charger les utilisateurs."));
    }
  }, []);

  useEffect(() => {
    let active = true;
    adminApiService.users()
      .then(({ data }) => {
        if (!active) return;
        setUsers(data || []);
        setError("");
      })
      .catch((err) => {
        console.error(err);
        if (active) setError(getRequestErrorMessage(err, "Impossible de charger les utilisateurs."));
      });

    return () => {
      active = false;
    };
  }, []);

  // =========================
  // OUVRIR MODAL AJOUT
  // =========================

  const openCreateModal = (role) => {
    setError("");
    setEditingUser(null);
    setModalRole(role);
    setModalOpen(true);
  };

  // =========================
  // OUVRIR MODAL MODIFICATION
  // =========================

  const openEditModal = (user) => {
    setError("");
    setEditingUser(user);
    setModalRole(user.role || "USER");
    setModalOpen(true);
  };

  const openHistory = (user) => setHistoryUser(user);

  // =========================
  // FERMER MODAL
  // =========================

  const closeModal = () => {
    if (saving) return;

    setModalOpen(false);
    setEditingUser(null);
  };

  // =========================
  // ENREGISTRER
  // =========================

  const handleSaveUser = async (data) => {
    try {
      setSaving(true);
      setError("");

      if (editingUser) {
        // MODIFICATION
        await adminApiService.updateUser(
          editingUser.id,
          data
        );
      } else {
        // AJOUT
        await adminApiService.createUser(data);
      }

      // Fermer le modal
      setModalOpen(false);
      setEditingUser(null);

      // Recharger les utilisateurs
      await loadUsers();

    } catch (err) {
      console.error(err);
      setError(getRequestErrorMessage(err, "Impossible d'enregistrer cet utilisateur."));
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteUser = async (user) => {
    const displayName = `${user.prenom || ""} ${user.nom || ""}`.trim() || user.email;
    if (!window.confirm(`Supprimer définitivement le compte de ${displayName} ?`)) return;

    try {
      setDeletingUserId(user.id);
      setError("");
      await adminApiService.deleteUser(user.id);
      setUsers((currentUsers) => currentUsers.filter((currentUser) => currentUser.id !== user.id));
    } catch (err) {
      console.error(err);
      setError(getRequestErrorMessage(err, "Impossible de supprimer cet utilisateur."));
    } finally {
      setDeletingUserId(null);
    }
  };

  // =========================
  // RECHERCHE
  // =========================

  const filteredUsers = useMemo(() => {
    const value = search.trim().toLowerCase();

    if (!value) {
      return users;
    }

    return users.filter((user) => {
      const fullName =
        `${user.prenom || ""} ${user.nom || ""}`.toLowerCase();

      return (
        fullName.includes(value) ||
        (user.nom || "").toLowerCase().includes(value) ||
        (user.prenom || "").toLowerCase().includes(value) ||
        (user.email || "").toLowerCase().includes(value)
      );
    });
  }, [users, search]);

  // =========================
  // SÉPARATION ADMIN / USERS
  // =========================

  const administrators = filteredUsers.filter(
    (user) => user.role?.toUpperCase() === "ADMIN"
  );

  const regularUsers = filteredUsers.filter(
    (user) => user.role?.toUpperCase() !== "ADMIN"
  );

  return (
    <section className="admin-users-page">

      {/* =========================
          HEADER
          ========================= */}

      <div className="admin-page-head">
        <div>
          <h1>Utilisateurs</h1>

          <p>
            {users.length} compte
            {users.length !== 1 ? "s" : ""} enregistré
            {users.length !== 1 ? "s" : ""}.
          </p>
        </div>
      </div>

      {/* =========================
          ERROR
          ========================= */}

      {error && (
        <p className="admin-error">
          {error}
        </p>
      )}

      {/* =========================
          SEARCH
          ========================= */}

      <div className="admin-users-search">
        <span>⌕</span>

        <input
          type="text"
          placeholder="Rechercher par nom ou email..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* =========================
          ADMINISTRATEURS
          ========================= */}

      <div className="admin-users-section">

        <div className="admin-users-section-head">

          <div>
            <h2>Administrateurs</h2>

            <span>
              {administrators.length}
            </span>
          </div>

          <button
            type="button"
            className="admin-primary-button"
            onClick={() => openCreateModal("ADMIN")}
          >
            + Ajouter un administrateur
          </button>

        </div>

        <div className="admin-card admin-table-wrap">

          <table className="admin-table">

            <thead>
              <tr>
                <th>Nom complet</th>
                <th>E-mail</th>
                <th>Rôle</th>
                <th>État</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>

              {administrators.map((user) => (

                <tr key={user.id}>

                  <td>
                    <strong>
                      {user.prenom} {user.nom}
                    </strong>
                  </td>

                  <td>
                    {user.email}
                  </td>

                  <td>
                    {user.role}
                  </td>

                  <td>

                    <span
                      className={
                        user.actif === false
                          ? "user-status inactive"
                          : "user-status active"
                      }
                    >
                      {user.actif === false
                        ? "Inactif"
                        : "Actif"}
                    </span>

                  </td>

                  <td>

                    <div className="user-actions">

                      <button type="button" onClick={() => openHistory(user)}>
                        Historique
                      </button>

                      <button
                        type="button"
                        onClick={() =>
                          openEditModal(user)
                        }
                      >
                        Modifier
                      </button>

                      <button
                        type="button"
                        className="user-delete-button"
                        onClick={() => handleDeleteUser(user)}
                        disabled={deletingUserId === user.id}
                      >
                        {deletingUserId === user.id ? "Suppression…" : "Supprimer"}
                      </button>

                    </div>

                  </td>

                </tr>

              ))}

              {administrators.length === 0 && (

                <tr>
                  <td
                    colSpan="5"
                    className="admin-muted"
                  >
                    Aucun administrateur.
                  </td>
                </tr>

              )}

            </tbody>

          </table>

        </div>

      </div>

      {/* =========================
          UTILISATEURS
          ========================= */}

      <div className="admin-users-section">

        <div className="admin-users-section-head">

          <div>
            <h2>Utilisateurs</h2>

            <span>
              {regularUsers.length}
            </span>
          </div>

          <button
            type="button"
            className="admin-primary-button"
            onClick={() => openCreateModal("USER")}
          >
            + Ajouter un utilisateur
          </button>

        </div>

        <div className="admin-card admin-table-wrap">

          <table className="admin-table">

            <thead>
              <tr>
                <th>Nom complet</th>
                <th>E-mail</th>
                <th>Rôle</th>
                <th>État</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>

              {regularUsers.map((user) => (

                <tr key={user.id}>

                  <td>
                    <strong>
                      {user.prenom} {user.nom}
                    </strong>
                  </td>

                  <td>
                    {user.email}
                  </td>

                  <td>
                    {user.role}
                  </td>

                  <td>

                    <span
                      className={
                        user.actif === false
                          ? "user-status inactive"
                          : "user-status active"
                      }
                    >
                      {user.actif === false
                        ? "Inactif"
                        : "Actif"}
                    </span>

                  </td>

                  <td>

                    <div className="user-actions">

                      <button type="button" onClick={() => openHistory(user)}>
                        Historique
                      </button>

                      <button
                        type="button"
                        onClick={() =>
                          openEditModal(user)
                        }
                      >
                        Modifier
                      </button>

                      <button
                        type="button"
                        className="user-delete-button"
                        onClick={() => handleDeleteUser(user)}
                        disabled={deletingUserId === user.id}
                      >
                        {deletingUserId === user.id ? "Suppression…" : "Supprimer"}
                      </button>

                    </div>

                  </td>

                </tr>

              ))}

              {regularUsers.length === 0 && (

                <tr>
                  <td
                    colSpan="5"
                    className="admin-muted"
                  >
                    Aucun utilisateur.
                  </td>
                </tr>

              )}

            </tbody>

          </table>

        </div>

      </div>

      {/* =========================
          MODAL
          ========================= */}

      {modalOpen && (

        <UserModal
          user={editingUser}
          role={modalRole}
          saving={saving}
          error={error}
          onClearError={() => setError("")}
          onClose={closeModal}
          onSave={handleSaveUser}
        />

      )}

      {historyUser && (
        <RentalHistoryModal
          user={historyUser}
          onClose={() => setHistoryUser(null)}
        />
      )}

    </section>
  );
}
