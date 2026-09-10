package com.hms.doctor.repository;

import com.hms.doctor.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByDoctorId(Long doctorId);
}
