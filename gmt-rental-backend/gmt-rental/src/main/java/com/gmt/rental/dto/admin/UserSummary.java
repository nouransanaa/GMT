package com.gmt.rental.dto.admin;

public record UserSummary(Long id, String nom, String prenom, String email, String role, Boolean actif) {}
