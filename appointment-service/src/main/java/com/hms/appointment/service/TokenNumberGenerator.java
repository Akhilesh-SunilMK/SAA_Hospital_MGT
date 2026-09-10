package com.hms.appointment.service;

import com.hms.appointment.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** FR-AP-08: OPD queue token numbers, e.g. "OPD-58-014" (SRS 6.2.4 sample response). */
@Component
public class TokenNumberGenerator {

    private final AppointmentRepository appointmentRepository;

    public TokenNumberGenerator(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public String next(Long doctorId, LocalDate date) {
        long countSoFar = appointmentRepository.countByDoctorIdAndSlotBetween(
                doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        long sequence = countSoFar + 1;
        return "OPD-" + doctorId + "-" + String.format("%03d", sequence);
    }
}
