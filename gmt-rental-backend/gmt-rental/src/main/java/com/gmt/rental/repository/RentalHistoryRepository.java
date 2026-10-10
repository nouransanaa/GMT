package com.gmt.rental.repository;

import com.gmt.rental.entity.RentalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalHistoryRepository extends JpaRepository<RentalHistory, Long> {
    List<RentalHistory> findAllByUserIdOrderByDateDebutDescIdDesc(Long userId);

    @Query("""
            SELECT rental FROM RentalHistory rental
            WHERE (rental.vehicleEntity.id = :vehicleId
                   OR (rental.vehicleEntity IS NULL
                       AND LOWER(rental.vehicle) = LOWER(:vehicleName)))
              AND rental.dateDebut < :dateFin
              AND rental.dateFin > :dateDebut
            ORDER BY rental.dateDebut, rental.dateFin
            """)
    List<RentalHistory> findDateOverlapsByVehicleId(
            @Param("vehicleId") Long vehicleId,
            @Param("vehicleName") String vehicleName,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);
}
