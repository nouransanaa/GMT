package com.gmt.rental.controller;

import com.gmt.rental.dto.QuoteRequestDTO;
import com.gmt.rental.entity.QuoteRequest;
import com.gmt.rental.service.QuoteRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/demandes")
@CrossOrigin(origins = "http://localhost:5174")
public class QuoteRequestController {

    private final QuoteRequestService quoteRequestService;

    public QuoteRequestController(
            QuoteRequestService quoteRequestService) {
        this.quoteRequestService = quoteRequestService;
    }

    @PostMapping
    public ResponseEntity<QuoteRequest> createQuote(
            @RequestBody QuoteRequestDTO dto) {

        QuoteRequest savedQuote =
                quoteRequestService.createQuote(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedQuote);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

}
