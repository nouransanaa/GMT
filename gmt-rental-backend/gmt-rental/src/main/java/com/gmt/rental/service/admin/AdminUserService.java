package com.gmt.rental.service.admin;

import com.gmt.rental.dto.admin.UserSummary;
import com.gmt.rental.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {
    private final UserRepository users;
    public AdminUserService(UserRepository users) { this.users = users; }
    public List<UserSummary> findAll() {
        return users.findAll().stream().map(user -> new UserSummary(
                user.getId(), user.getNom(), user.getPrenom(), user.getEmail(), user.getRole(), user.getActif()
        )).toList();
    }
}
