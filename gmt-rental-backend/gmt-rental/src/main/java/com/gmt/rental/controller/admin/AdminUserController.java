package com.gmt.rental.controller.admin;

import com.gmt.rental.dto.admin.UserSummary;
import com.gmt.rental.entity.User;
import com.gmt.rental.service.admin.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserSummary> getAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary create(@RequestBody User user) {
        return service.create(user);
    }

    @PutMapping("/{id}")
    public UserSummary update(
            @PathVariable Long id,
            @RequestBody User user
    ) {
        return service.update(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/resend-verification")
    public void resendVerification(@PathVariable Long id) {
        service.resendVerification(id);
    }
}
