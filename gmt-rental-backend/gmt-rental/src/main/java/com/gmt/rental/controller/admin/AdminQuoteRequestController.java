package com.gmt.rental.controller.admin;

import com.gmt.rental.entity.QuoteRequest;
import com.gmt.rental.service.admin.AdminQuoteRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/demandes")
public class AdminQuoteRequestController {
    private final AdminQuoteRequestService service;
    public AdminQuoteRequestController(AdminQuoteRequestService service) { this.service = service; }
    @GetMapping public List<QuoteRequest> getAll(@RequestParam(required = false) String statut) { return service.findAll(statut); }
    @GetMapping("/{id}") public QuoteRequest getById(@PathVariable Long id) { return service.findById(id); }
    @PatchMapping("/{id}/statut") public QuoteRequest updateStatus(@PathVariable Long id, @RequestParam String statut) { return service.updateStatus(id, statut); }
}
