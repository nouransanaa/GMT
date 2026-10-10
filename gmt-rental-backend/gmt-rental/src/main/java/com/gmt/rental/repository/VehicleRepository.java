package com.gmt.rental.repository;

import com.gmt.rental.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByStatut(String statut);

    List<Vehicle> findByCategorieId(Long categorieId);

    List<Vehicle> findByStatutAndCategorieId(String statut, Long categorieId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT vehicle FROM Vehicle vehicle WHERE vehicle.id = :id")
    Optional<Vehicle> findByIdForUpdate(@Param("id") Long id);
}
