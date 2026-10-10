package com.gmt.rental.repository;

import com.gmt.rental.entity.QuoteRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
    @Query("""
            SELECT request FROM QuoteRequest request
            WHERE request.vehicule.id = :vehicleId
              AND request.dateDebut < :dateFin
              AND request.dateFin > :dateDebut
            ORDER BY request.dateDebut, request.dateFin
            """)
    List<QuoteRequest> findDateOverlapsByVehicleId(
            @Param("vehicleId") Long vehicleId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);
}
