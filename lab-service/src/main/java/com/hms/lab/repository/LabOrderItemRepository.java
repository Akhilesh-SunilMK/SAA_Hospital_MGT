package com.hms.lab.repository;

import com.hms.lab.entity.LabOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabOrderItemRepository extends JpaRepository<LabOrderItem, Long> {
}
