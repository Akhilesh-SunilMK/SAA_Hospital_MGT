package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByDoctorIdAndSlotAndStatusNot(Long doctorId, LocalDateTime slot, AppointmentStatus excludedStatus);

    long countByDoctorIdAndSlotBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByDoctorIdAndSlotBetweenAndStatusInOrderBySlotAsc(
            Long doctorId, LocalDateTime start, LocalDateTime end, List<AppointmentStatus> statuses);

    List<Appointment> findByStatusAndSlotBefore(AppointmentStatus status, LocalDateTime cutoff);

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status, Pageable pageable);

    Page<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status, Pageable pageable);
}
