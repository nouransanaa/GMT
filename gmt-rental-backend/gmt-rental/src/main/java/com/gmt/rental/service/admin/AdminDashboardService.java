package com.gmt.rental.service.admin;

import com.gmt.rental.dto.admin.DashboardSummary;
import com.gmt.rental.repository.CategoryRepository;
import com.gmt.rental.repository.QuoteRequestRepository;
import com.gmt.rental.repository.VehicleRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {
    private final VehicleRepository vehicles;
    private final CategoryRepository categories;
    private final QuoteRequestRepository requests;

    public AdminDashboardService(VehicleRepository vehicles, CategoryRepository categories, QuoteRequestRepository requests) {
        this.vehicles = vehicles;
        this.categories = categories;
        this.requests = requests;
    }

    public DashboardSummary summary() {
        long totalRequests = requests.count();
        long pending = requests.findAll().stream().filter(r -> "NOUVELLE".equalsIgnoreCase(r.getStatut())).count();
        return new DashboardSummary(vehicles.count(), categories.count(), pending, totalRequests);
    }
}
