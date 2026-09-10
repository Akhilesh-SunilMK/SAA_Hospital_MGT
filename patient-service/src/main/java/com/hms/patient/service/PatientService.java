package com.hms.patient.service;

import com.hms.patient.entity.Admission;
import com.hms.patient.entity.EmergencyContact;
import com.hms.patient.entity.Patient;
import com.hms.patient.entity.PatientAllergy;
import com.hms.patient.repository.AdmissionRepository;
import com.hms.patient.repository.PatientRepository;
import com.hms.common.exception.ConflictException;
import com.hms.common.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PatientService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PatientRepository patientRepository;
    private final AdmissionRepository admissionRepository;

    public PatientService(PatientRepository patientRepository, AdmissionRepository admissionRepository) {
        this.patientRepository = patientRepository;
        this.admissionRepository = admissionRepository;
    }

    @Transactional
    public Patient register(com.hms.patient.dto.PatientRequest req) {
        String mrn = generateUniqueMrn();
        Patient patient = Patient.builder()
                .mrn(mrn)
                .userId(req.userId())
                .firstName(req.firstName())
                .lastName(req.lastName())
                .dob(req.dob())
                .gender(req.gender())
                .phone(req.phone())
                .email(req.email())
                .address(req.address())
                .bloodGroup(req.bloodGroup())
                .allergiesSummary(req.allergiesSummary())
                .chronicConditions(req.chronicConditions())
                .build();
        try {
            return patientRepository.save(patient);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Failed to allocate a unique MRN, please retry");
        }
    }

    private String generateUniqueMrn() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = "MRN-" + datePart + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (!patientRepository.existsByMrn(candidate)) {
                return candidate;
            }
        }
        throw new ConflictException("Unable to generate a unique MRN, please retry");
    }

    public Patient getById(Long id) {
        return patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
    }

    public void assertOwnerOrStaff(Patient patient, Long callerUserId, boolean isStaff) {
        if (isStaff) {
            return;
        }
        if (callerUserId == null || patient.getUserId() == null || !callerUserId.equals(patient.getUserId())) {
            throw new AccessDeniedException("You may only access your own patient record");
        }
    }

    public Page<Patient> search(String term, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize <= 0 ? DEFAULT_PAGE_SIZE : safeSize);
        if (term == null || term.isBlank()) {
            return patientRepository.findAll(pageable);
        }
        return patientRepository.search(term.trim(), pageable);
    }

    @Transactional
    public Patient update(Long id, com.hms.patient.dto.PatientUpdateRequest req) {
        Patient patient = getById(id);
        patient.updateContactDetails(req.phone(), req.email(), req.address());
        patient.updateClinicalSummary(req.bloodGroup(), req.allergiesSummary(), req.chronicConditions());
        return patient;
    }

    @Transactional
    public void softDelete(Long id) {
        Patient patient = getById(id);
        patient.markDeleted();
    }

    @Transactional
    public PatientAllergy addAllergy(Long patientId, com.hms.patient.dto.AllergyRequest req) {
        Patient patient = getById(patientId);
        PatientAllergy allergy = new PatientAllergy(req.allergen(), req.severity(), req.notedOn());
        patient.addAllergy(allergy);
        return allergy;
    }

    @Transactional
    public EmergencyContact addEmergencyContact(Long patientId, com.hms.patient.dto.EmergencyContactRequest req) {
        Patient patient = getById(patientId);
        EmergencyContact contact = new EmergencyContact(req.name(), req.relation(), req.phone());
        patient.addEmergencyContact(contact);
        return contact;
    }

    @Transactional
    public Admission admit(Long patientId, com.hms.patient.dto.AdmitRequest req) {
        Patient patient = getById(patientId);
        admissionRepository.findFirstByPatientIdAndStatus(patientId, Admission.Status.ADMITTED)
                .ifPresent(a -> { throw new ConflictException("Patient is already admitted"); });
        Admission admission = new Admission(patient, req.wardId(), req.bedNo());
        return admissionRepository.save(admission);
    }

    @Transactional
    public Admission discharge(Long patientId) {
        Admission admission = admissionRepository.findFirstByPatientIdAndStatus(patientId, Admission.Status.ADMITTED)
                .orElseThrow(() -> new ResourceNotFoundException("No active admission for patient: " + patientId));
        admission.discharge();
        return admission;
    }
}
