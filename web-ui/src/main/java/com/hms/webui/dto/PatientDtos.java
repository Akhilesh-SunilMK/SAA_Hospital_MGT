package com.hms.webui.dto;

import java.time.LocalDate;

public final class PatientDtos {
    private PatientDtos() {}

    public record PatientRequest(String firstName, String lastName, LocalDate dob, String gender,
                                  String phone, String email, String address, String bloodGroup,
                                  String allergiesSummary, String chronicConditions, Long userId) {}

    public record PatientUpdateRequest(String phone, String email, String address, String bloodGroup,
                                        String allergiesSummary, String chronicConditions) {}

    public record PatientResponse(Long id, String mrn, Long userId, String firstName, String lastName,
                                   LocalDate dob, String gender, String bloodGroup, String phone, String email,
                                   String address, String allergiesSummary, String chronicConditions,
                                   String createdAt, boolean deleted) {}

    public record AdmitRequest(Long wardId, String bedNo) {}

    public record AdmissionResponse(Long id, Long patientId, Long wardId, String bedNo,
                                     String admittedAt, String dischargedAt, String status) {}

    public record AllergyRequest(String allergen, String severity, LocalDate notedOn) {}

    public record AllergyResponse(Long id, String allergen, String severity, LocalDate notedOn) {}

    public record EmergencyContactRequest(String name, String relation, String phone) {}

    public record EmergencyContactResponse(Long id, String name, String relation, String phone) {}
}
