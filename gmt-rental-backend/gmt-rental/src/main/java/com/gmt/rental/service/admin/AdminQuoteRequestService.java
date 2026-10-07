package com.gmt.rental.service.admin;

import com.gmt.rental.entity.QuoteRequest;
import com.gmt.rental.repository.QuoteRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class AdminQuoteRequestService {
    private final QuoteRequestRepository requests;
    public AdminQuoteRequestService(QuoteRequestRepository requests) { this.requests = requests; }

    public List<QuoteRequest> findAll(String status) {
        return requests.findAll().stream()
                .filter(request -> status == null || status.isBlank() || status.equalsIgnoreCase(request.getStatut()))
                .sorted(Comparator.comparing(QuoteRequest::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public QuoteRequest findById(Long id) { return requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande de devis introuvable")); }

    public QuoteRequest updateStatus(Long id, String status) {
        if (!List.of("NOUVELLE", "EN_COURS", "TRAITEE", "ANNULEE").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut de demande invalide");
        }
        QuoteRequest request = findById(id);
        request.setStatut(status);
        return requests.save(request);
    }
}
