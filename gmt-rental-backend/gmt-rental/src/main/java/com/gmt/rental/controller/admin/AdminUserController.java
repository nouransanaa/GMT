package com.gmt.rental.controller.admin;

import com.gmt.rental.dto.admin.UserSummary;
import com.gmt.rental.service.admin.AdminUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService service;
    public AdminUserController(AdminUserService service) { this.service = service; }
    @GetMapping public List<UserSummary> getAll() { return service.findAll(); }
}
