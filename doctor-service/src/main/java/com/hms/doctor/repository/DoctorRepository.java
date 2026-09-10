package com.hms.doctor.repository;

import com.hms.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialisationIgnoreCaseAndDepartmentIgnoreCase(String specialisation, String department);
    List<Doctor> findBySpecialisationIgnoreCase(String specialisation);
    List<Doctor> findByDepartmentIgnoreCase(String department);
}
