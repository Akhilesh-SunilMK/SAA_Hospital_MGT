package com.hms.doctor.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.doctor.dto.*;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.entity.Leave;
import com.hms.doctor.entity.Schedule;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.doctor.repository.LeaveRepository;
import com.hms.doctor.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final LeaveRepository leaveRepository;

    public DoctorService(DoctorRepository doctorRepository, ScheduleRepository scheduleRepository,
                          LeaveRepository leaveRepository) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.leaveRepository = leaveRepository;
    }

    @Transactional
    public Doctor create(DoctorRequest req) {
        Doctor doctor = new Doctor(req.userId(), req.firstName(), req.lastName(), req.registrationNo(),
                req.qualification(), req.specialisation(), req.department(), req.consultationFee());
        return doctorRepository.save(doctor);
    }

    public Doctor getById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }

    public List<Doctor> filter(String specialisation, String department) {
        boolean hasSpec = StringUtils.hasText(specialisation);
        boolean hasDept = StringUtils.hasText(department);
        if (hasSpec && hasDept) {
            return doctorRepository.findBySpecialisationIgnoreCaseAndDepartmentIgnoreCase(specialisation, department);
        }
        if (hasSpec) {
            return doctorRepository.findBySpecialisationIgnoreCase(specialisation);
        }
        if (hasDept) {
            return doctorRepository.findByDepartmentIgnoreCase(department);
        }
        return doctorRepository.findAll();
    }

    @Transactional
    public List<Schedule> replaceSchedule(Long doctorId, ScheduleRequest req) {
        Doctor doctor = getById(doctorId);
        scheduleRepository.deleteByDoctorId(doctorId);
        List<Schedule> schedules = new ArrayList<>();
        for (ScheduleRequest.ScheduleSlot slot : req.slots()) {
            Schedule schedule = Schedule.builder()
                    .doctor(doctor)
                    .dayOfWeek(DayOfWeek.valueOf(slot.dayOfWeek().toUpperCase()))
                    .startTime(LocalTime.parse(slot.startTime()))
                    .endTime(LocalTime.parse(slot.endTime()))
                    .slotDurationMin(slot.slotDurationMin())
                    .build();
            schedules.add(scheduleRepository.save(schedule));
        }
        return schedules;
    }

    @Transactional
    public Leave markLeave(Long doctorId, LeaveRequest req) {
        Doctor doctor = getById(doctorId);
        Leave leave = new Leave(doctor, req.fromDate(), req.toDate(), req.reason());
        return leaveRepository.save(leave);
        // FR-DR-04: rescheduling notifications are appointment-service's responsibility once it
        // re-checks availability against this leave window and finds a conflict.
    }

    public AvailabilityResponse availability(Long doctorId, LocalDate date) {
        Doctor doctor = getById(doctorId);
        boolean onLeave = leaveRepository.findByDoctorId(doctorId).stream().anyMatch(l -> l.covers(date));
        if (onLeave) {
            return new AvailabilityResponse(doctorId, date, true, List.of());
        }
        List<Schedule> daySchedules = scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek());
        List<LocalTime> slots = new ArrayList<>();
        for (Schedule schedule : daySchedules) {
            LocalTime cursor = schedule.getStartTime();
            while (cursor.plusMinutes(schedule.getSlotDurationMin()).compareTo(schedule.getEndTime()) <= 0) {
                slots.add(cursor);
                cursor = cursor.plusMinutes(schedule.getSlotDurationMin());
            }
        }
        return new AvailabilityResponse(doctorId, date, false, slots);
    }
}
