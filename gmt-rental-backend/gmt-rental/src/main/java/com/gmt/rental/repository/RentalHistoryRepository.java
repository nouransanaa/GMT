package com.gmt.rental.repository;

import com.gmt.rental.entity.RentalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentalHistoryRepository extends JpaRepository<RentalHistory, Long> {
    List<RentalHistory> findAllByUserIdOrderByDateDebutDescIdDesc(Long userId);
}
