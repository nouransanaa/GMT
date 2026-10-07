package com.gmt.rental.repository;

import com.gmt.rental.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByStatut(String statut);

    List<Vehicle> findByCategorieId(Long categorieId);

    List<Vehicle> findByStatutAndCategorieId(String statut, Long categorieId);
}
