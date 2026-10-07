package com.gmt.rental.service;

import com.gmt.rental.entity.Vehicle;
import com.gmt.rental.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getActiveVehicles() {
        return vehicleRepository.findByStatut("ACTIF");
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public Optional<Vehicle> getPublicVehicleById(Long id) {
        return vehicleRepository.findById(id).filter(vehicle -> "ACTIF".equalsIgnoreCase(vehicle.getStatut()));
    }

    public List<Vehicle> getVehiclesByCategory(Long categorieId) {
        return vehicleRepository.findByCategorieId(categorieId);
    }

    public List<Vehicle> searchPublicVehicles(String statut, Long categorieId) {
        String requestedStatus = statut == null || statut.isBlank() ? "ACTIF" : statut.trim().toUpperCase();
        if (categorieId != null) {
            return vehicleRepository.findByStatutAndCategorieId(requestedStatus, categorieId);
        }
        return vehicleRepository.findByStatut(requestedStatus);
    }
}
