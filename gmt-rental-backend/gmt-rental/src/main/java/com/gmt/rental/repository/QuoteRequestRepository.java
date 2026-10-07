package com.gmt.rental.repository;

import com.gmt.rental.entity.QuoteRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
}
