package com.gmt.rental.service.admin;

import com.gmt.rental.dto.admin.UserSummary;
import com.gmt.rental.entity.User;
import com.gmt.rental.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AdminUserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN");

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserSummary> findAll() {
        return users.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    public UserSummary create(User data) {
        validate(data, true);
        ensureEmailAvailable(data.getEmail(), null);

        User user = new User();
        copyProfile(data, user);
        user.setRole(data.getRole());
        user.setMotDePasse(passwordEncoder.encode(data.getMotDePasse()));
        user.setActif(data.getActif() == null || data.getActif());

        return toSummary(save(user));
    }

    public UserSummary update(Long id, User data) {
        User user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable : " + id));

        validate(data, false);
        ensureEmailAvailable(data.getEmail(), id);
        copyProfile(data, user);

        if (data.getActif() != null) user.setActif(data.getActif());
        if (data.getMotDePasse() != null && !data.getMotDePasse().isBlank()) {
            user.setMotDePasse(passwordEncoder.encode(data.getMotDePasse()));
        }

        return toSummary(save(user));
    }

    public void delete(Long id) {
        User user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable : " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null
                && authentication.getName().equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous ne pouvez pas supprimer le compte administrateur connecté.");
        }

        if (isAdmin(user) && adminCount() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de supprimer le dernier administrateur.");
        }

        users.delete(user);
    }

    private boolean isAdmin(User user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase(Locale.ROOT);
        return role.equals("ADMIN") || role.equals("ROLE_ADMIN");
    }

    private long adminCount() {
        return users.countByRoleIgnoreCase("ADMIN") + users.countByRoleIgnoreCase("ROLE_ADMIN");
    }

    private void validate(User data, boolean creating) {
        if (data == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les données utilisateur sont obligatoires.");
        }
        if (data.getPrenom() == null || data.getPrenom().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le prénom est obligatoire.");
        }
        if (data.getNom() == null || data.getNom().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom est obligatoire.");
        }
        if (data.getEmail() == null || !EMAIL_PATTERN.matcher(data.getEmail().trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une adresse e-mail valide est obligatoire.");
        }
        if (creating && (data.getMotDePasse() == null || data.getMotDePasse().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe est obligatoire pour créer un compte.");
        }
        if (creating && data.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le rôle doit être USER ou ADMIN.");
        }
        if (creating) {
            String role = data.getRole().trim().toUpperCase(Locale.ROOT);
            if (role.startsWith("ROLE_")) role = role.substring("ROLE_".length());
            if (!ALLOWED_ROLES.contains(role)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le rôle doit être USER ou ADMIN.");
            }
            data.setRole(role);
        }

        data.setNom(data.getNom().trim());
        data.setPrenom(data.getPrenom().trim());
        data.setEmail(data.getEmail().trim().toLowerCase(Locale.ROOT));
    }

    private void ensureEmailAvailable(String email, Long currentUserId) {
        users.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (currentUserId == null || !currentUserId.equals(existing.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse e-mail est déjà utilisée.");
            }
        });
    }

    private void copyProfile(User source, User target) {
        target.setNom(source.getNom());
        target.setPrenom(source.getPrenom());
        target.setEmail(source.getEmail());
    }

    private User save(User user) {
        try {
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse e-mail est déjà utilisée.", exception);
        }
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole(),
                user.getActif()
        );
    }
}
