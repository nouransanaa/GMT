package com.gmt.rental.service;

import com.gmt.rental.dto.QuoteRequestDTO;
import com.gmt.rental.entity.QuoteRequest;
import com.gmt.rental.entity.Vehicle;
import com.gmt.rental.repository.QuoteRequestRepository;
import com.gmt.rental.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class QuoteRequestService {

    private final QuoteRequestRepository quoteRequestRepository;
    private final VehicleRepository vehicleRepository;

    public QuoteRequestService(
            QuoteRequestRepository quoteRequestRepository,
            VehicleRepository vehicleRepository) {

        this.quoteRequestRepository = quoteRequestRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public QuoteRequest createQuote(QuoteRequestDTO dto) {

        if (dto == null || dto.getVehiculeId() == null) {
            throw new IllegalArgumentException("Veuillez sélectionner un véhicule.");
        }
        if (isBlank(dto.getNom()) || isBlank(dto.getPrenom()) || isBlank(dto.getEmail()) || isBlank(dto.getTelephone())) {
            throw new IllegalArgumentException("Le nom, le prénom, l’email et le téléphone sont obligatoires.");
        }
        if (!dto.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("L’adresse email est invalide.");
        }
        if (isBlank(dto.getLieuPriseEnCharge()) || isBlank(dto.getLieuRestitution())) {
            throw new IllegalArgumentException("Les lieux de prise en charge et de restitution sont obligatoires.");
        }
        if (dto.getDateDebut() == null || dto.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires.");
        }
        if (dto.getDateDebut().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de début ne peut pas être dans le passé.");
        }

        // Chercher le véhicule
        Vehicle vehicle = vehicleRepository
                .findById(dto.getVehiculeId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Le véhicule sélectionné n'existe plus. Veuillez en choisir un autre."));

        // Vérifier les dates
        long nombreJours = ChronoUnit.DAYS.between(
                dto.getDateDebut(),
                dto.getDateFin()
        );

        if (nombreJours <= 0) {
            throw new IllegalArgumentException(
                    "La date de fin doit être après la date de début"
            );
        }

        // Le tarif peut être absent pour une demande de devis : le prix sera
        // déterminé par l'équipe, et le total restera null jusque-là.
        BigDecimal prixTotal = vehicle.getPrixParJour() == null
                ? null
                : vehicle.getPrixParJour().multiply(BigDecimal.valueOf(nombreJours));

        // Créer l'entité
        QuoteRequest quoteRequest = new QuoteRequest();

        quoteRequest.setNom(dto.getNom());
        quoteRequest.setPrenom(dto.getPrenom());
        quoteRequest.setEmail(dto.getEmail());
        quoteRequest.setTelephone(dto.getTelephone());

        quoteRequest.setVehicule(vehicle);

        quoteRequest.setDateDebut(dto.getDateDebut());
        quoteRequest.setDateFin(dto.getDateFin());

        quoteRequest.setLieuPriseEnCharge(
                dto.getLieuPriseEnCharge()
        );

        quoteRequest.setLieuRestitution(
                dto.getLieuRestitution()
        );

        quoteRequest.setMessage(dto.getMessage());

        quoteRequest.setPrixTotal(prixTotal);

        // Sauvegarder dans PostgreSQL
        return quoteRequestRepository.save(quoteRequest);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
