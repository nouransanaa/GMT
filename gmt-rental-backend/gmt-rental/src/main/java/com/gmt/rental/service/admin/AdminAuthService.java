package com.gmt.rental.service.admin;

import com.gmt.rental.dto.admin.LoginRequest;
import com.gmt.rental.dto.admin.LoginResponse;
import com.gmt.rental.entity.User;
import com.gmt.rental.repository.UserRepository;
import com.gmt.rental.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AdminAuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.email() == null || request.motDePasse() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail et mot de passe requis");
        }
        User user = users.findByEmail(request.email().trim()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        boolean admin = role.equals("ADMIN") || role.equals("ROLE_ADMIN");
        if (!admin || Boolean.FALSE.equals(user.getActif()) || user.getMotDePasse() == null
                || !passwordEncoder.matches(request.motDePasse(), user.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        return new LoginResponse(jwtUtils.generateToken(user.getEmail()), "Bearer", user.getId(), user.getEmail(), user.getNom(), user.getPrenom(), "ROLE_ADMIN");
    }
}
