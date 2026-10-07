export function requestStatusBadgeClass(status) {
  const variant = {
    NOUVELLE: "warning",
    EN_COURS: "info",
    TRAITEE: "success",
    ANNULEE: "danger",
  }[status] || "neutral";

  return `admin-badge admin-badge-${variant}`;
}
