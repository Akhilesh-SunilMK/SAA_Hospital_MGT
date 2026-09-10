package com.hms.appointment.repository;

import com.hms.appointment.entity.SlotReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SlotReservationRepository extends JpaRepository<SlotReservation, Long> {

    Optional<SlotReservation> findByAppointmentId(Long appointmentId);

    void deleteByDoctorIdAndSlot(Long doctorId, LocalDateTime slot);
}
