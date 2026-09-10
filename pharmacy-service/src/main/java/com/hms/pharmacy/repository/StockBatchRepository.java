package com.hms.pharmacy.repository;

import com.hms.pharmacy.entity.StockBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {

    List<StockBatch> findByDrugIdAndQuantityGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
            Long drugId, Integer quantity, LocalDate today);

    List<StockBatch> findByDrugId(Long drugId);
}
