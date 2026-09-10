package com.hms.pharmacy.repository;

import com.hms.pharmacy.entity.DispenseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispenseOrderRepository extends JpaRepository<DispenseOrder, Long> {
}
