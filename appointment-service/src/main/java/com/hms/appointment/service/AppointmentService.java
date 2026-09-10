package com.hms.appointment.service;

import com.hms.appointment.dto.AppointmentResponse;
import com.hms.appointment.dto.BookAppointmentRequest;
import com.hms.appointment.dto.CreateInvoiceRequest;
import com.hms.appointment.dto.DoctorSummary;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.AppointmentStatus;
import com.hms.appointment.entity.AppointmentType;
import com.hms.appointment.factory.SlotAllocationStrategy;
import com.hms.appointment.factory.SlotAllocatorFactory;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.appointment.repository.SlotReservationRepository;
import com.hms.appointment.entity.SlotReservation;
import com.hms.common.event.EventPublisher;
import com.hms.common.exception.BusinessRuleException;
import com.hms.common.exception.ConflictException;
import com.hms.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the booking Saga (SRS 3.4): BookAppointment -> ReserveSlot -> CreateProvisionalInvoice.
 * Local writes (appointment + slot_reservations) are compensated for free by @Transactional
 * rollback if the remote CreateProvisionalInvoice step fails — there is nothing to "CancelInvoice"
 * because the invoice was never actually created in that failure path.
 */
@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private static final BigDecimal DEFAULT_CONSULTATION_FEE = BigDecimal.valueOf(500);

    private final AppointmentRepository appointmentRepository;
    private final SlotReservationRepository slotReservationRepository;
    private final SlotAllocatorFactory slotAllocatorFactory;
    private final TokenNumberGenerator tokenNumberGenerator;
    private final DownstreamIntegrationService downstreamIntegrationService;
    private final EventPublisher eventPublisher;

    @Value("${hms.appointment.cancellation-window-minutes:60}")
    private long cancellationWindowMinutes;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               SlotReservationRepository slotReservationRepository,
                               SlotAllocatorFactory slotAllocatorFactory,
                               TokenNumberGenerator tokenNumberGenerator,
                               DownstreamIntegrationService downstreamIntegrationService,
                               EventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.slotReservationRepository = slotReservationRepository;
        this.slotAllocatorFactory = slotAllocatorFactory;
        this.tokenNumberGenerator = tokenNumberGenerator;
        this.downstreamIntegrationService = downstreamIntegrationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AppointmentResponse book(BookAppointmentRequest req) {
        // Validate referenced aggregates exist (SRS 3.3 synchronous validation calls).
        downstreamIntegrationService.fetchPatient(req.patientId());
        DoctorSummary doctor = downstreamIntegrationService.fetchDoctor(req.doctorId());

        SlotAllocationStrategy strategy = slotAllocatorFactory.resolve(req.type());

        // FR-AP-01: the slot must actually be offered by the doctor. EMERGENCY bypasses this
        // along with the uniqueness check (FR-AP-06 "bypasses normal slot restrictions").
        if (strategy.requiresSlotUniquenessCheck()) {
            var availability = downstreamIntegrationService.fetchAvailability(req.doctorId(), req.slot().toLocalDate());
            if (availability == null || availability.onLeave()) {
                throw new BusinessRuleException("Doctor is not available on the requested date");
            }
            boolean offered = availability.availableSlots() != null
                    && availability.availableSlots().contains(req.slot().toLocalTime());
            if (!offered) {
                throw new BusinessRuleException("Requested slot is not offered by this doctor");
            }
        }

        if (strategy.requiresSlotUniquenessCheck()) {
            appointmentRepository.findByDoctorIdAndSlotAndStatusNot(req.doctorId(), req.slot(), AppointmentStatus.CANCELLED)
                    .ifPresent(existing -> {
                        throw new ConflictException("Slot already booked for this doctor");
                    });
        }

        String tokenNumber = tokenNumberGenerator.next(req.doctorId(), req.slot().toLocalDate());

        Appointment appointment = Appointment.builder()
                .patientId(req.patientId())
                .doctorId(req.doctorId())
                .slot(req.slot())
                .type(req.type())
                .reason(req.reason())
                .durationMinutes(req.durationMinutes())
                .referredBy(req.referredByDoctorId())
                .insuranceClaimed(req.insuranceClaimed() != null && req.insuranceClaimed())
                .build();
        appointment.assignTokenNumber(tokenNumber);

        Appointment saved = appointmentRepository.save(appointment);

        if (strategy.requiresSlotUniquenessCheck()) {
            try {
                // saveAndFlush forces the unique-index violation to surface synchronously here,
                // inside this transaction, instead of at a deferred flush we could no longer react to.
                slotReservationRepository.saveAndFlush(
                        new SlotReservation(req.doctorId(), req.slot(), saved.getId()));
            } catch (DataIntegrityViolationException e) {
                throw new ConflictException("Slot already booked for this doctor");
            }
        }

        BigDecimal fee = (doctor != null && doctor.consultationFee() != null) ? doctor.consultationFee() : DEFAULT_CONSULTATION_FEE;
        try {
            downstreamIntegrationService.createProvisionalInvoice(new CreateInvoiceRequest(
                    req.patientId(), saved.getId(), "CONSULTATION",
                    "Consultation fee - appointment #" + saved.getId(), fee));
        } catch (RuntimeException e) {
            // Saga compensation: throwing here rolls back the appointment + slot_reservations
            // insert above via @Transactional (ReleaseSlot). There is no invoice to cancel since
            // creation itself failed.
            log.error("Saga compensation triggered for appointment booking (patient={}, doctor={}, slot={}): {}",
                    req.patientId(), req.doctorId(), req.slot(), e.toString());
            throw e;
        }

        eventPublisher.publish("appointment.confirmed", Map.of(
                "appointmentId", saved.getId(),
                "patientId", saved.getPatientId(),
                "doctorId", saved.getDoctorId(),
                "slot", saved.getSlot().toString(),
                "tokenNumber", tokenNumber,
                "type", saved.getType().name()
        ));

        String doctorName = doctor != null ? doctor.fullName() : null;
        return AppointmentResponse.from(saved, doctorName);
    }

    @Transactional(readOnly = true)
    public Appointment getOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Appointment> query(Long patientId, Long doctorId, AppointmentStatus status, Pageable pageable) {
        if (patientId != null && status != null) {
            return appointmentRepository.findByPatientIdAndStatus(patientId, status, pageable);
        }
        if (doctorId != null && status != null) {
            return appointmentRepository.findByDoctorIdAndStatus(doctorId, status, pageable);
        }
        if (patientId != null) {
            return appointmentRepository.findByPatientId(patientId, pageable);
        }
        if (doctorId != null) {
            return appointmentRepository.findByDoctorId(doctorId, pageable);
        }
        return appointmentRepository.findAll(pageable);
    }

    @Transactional
    public Appointment reschedule(Long id, LocalDateTime newSlot) {
        Appointment appointment = getOrThrow(id);
        enforceCancellationWindow(appointment);

        SlotAllocationStrategy strategy = slotAllocatorFactory.resolve(appointment.getType());
        if (strategy.requiresSlotUniquenessCheck()) {
            appointmentRepository.findByDoctorIdAndSlotAndStatusNot(appointment.getDoctorId(), newSlot, AppointmentStatus.CANCELLED)
                    .ifPresent(existing -> {
                        throw new ConflictException("Target slot already booked for this doctor");
                    });
            slotReservationRepository.findByAppointmentId(appointment.getId())
                    .ifPresent(slotReservationRepository::delete);
            try {
                slotReservationRepository.saveAndFlush(
                        new SlotReservation(appointment.getDoctorId(), newSlot, appointment.getId()));
            } catch (DataIntegrityViolationException e) {
                throw new ConflictException("Target slot already booked for this doctor");
            }
        }

        appointment.reschedule(newSlot);
        return appointment;
    }

    @Transactional
    public void cancel(Long id) {
        Appointment appointment = getOrThrow(id);
        enforceCancellationWindow(appointment);
        appointment.cancel();
        slotReservationRepository.findByAppointmentId(appointment.getId())
                .ifPresent(slotReservationRepository::delete);
    }

    @Transactional
    public Appointment updateStatus(Long id, AppointmentStatus status) {
        Appointment appointment = getOrThrow(id);
        appointment.changeStatus(status);
        return appointment;
    }

    @Transactional(readOnly = true)
    public List<Appointment> liveQueue(Long doctorId) {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByDoctorIdAndSlotBetweenAndStatusInOrderBySlotAsc(
                doctorId, today.atStartOfDay(), today.plusDays(1).atStartOfDay(),
                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CHECKED_IN));
    }

    @Transactional
    public void markNoShows() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<Appointment> overdue = appointmentRepository.findByStatusAndSlotBefore(AppointmentStatus.SCHEDULED, cutoff);
        overdue.forEach(Appointment::markNoShow);
        if (!overdue.isEmpty()) {
            log.info("Marked {} appointment(s) as NO_SHOW", overdue.size());
        }
    }

    private void enforceCancellationWindow(Appointment appointment) {
        if (appointment.getSlot().minusMinutes(cancellationWindowMinutes).isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Cannot modify an appointment within " + cancellationWindowMinutes + " minutes of its slot");
        }
    }
}
