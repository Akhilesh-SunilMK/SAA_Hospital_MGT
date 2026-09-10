package com.hms.patient.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FR-PT-03: constructed exclusively via {@link Builder} — many optional fields (blood group,
 * email, address, allergy/condition summaries) make a telescoping constructor unreadable and a
 * setter-based approach would allow invalid intermediate state (SRS 4.3, 4.3.2, NFR-12).
 * No public setters: mutation happens only through intention-revealing methods.
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String mrn;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 500)
    private String address;

    @Column(name = "allergies_summary", length = 500)
    private String allergiesSummary;

    @Column(name = "chronic_conditions", length = 500)
    private String chronicConditions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean deleted;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PatientAllergy> allergies = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    protected Patient() {
        // JPA only
    }

    private Patient(Builder b) {
        this.mrn = b.mrn;
        this.userId = b.userId;
        this.firstName = b.firstName;
        this.lastName = b.lastName;
        this.dob = b.dob;
        this.gender = b.gender;
        this.bloodGroup = b.bloodGroup;
        this.phone = b.phone;
        this.email = b.email;
        this.address = b.address;
        this.allergiesSummary = b.allergiesSummary;
        this.chronicConditions = b.chronicConditions;
        this.createdAt = LocalDateTime.now();
        this.deleted = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String mrn;
        private Long userId;
        private String firstName;
        private String lastName;
        private LocalDate dob;
        private String gender;
        private String bloodGroup;
        private String phone;
        private String email;
        private String address;
        private String allergiesSummary;
        private String chronicConditions;

        public Builder mrn(String v) { this.mrn = v; return this; }
        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder firstName(String v) { this.firstName = v; return this; }
        public Builder lastName(String v) { this.lastName = v; return this; }
        public Builder dob(LocalDate v) { this.dob = v; return this; }
        public Builder gender(String v) { this.gender = v; return this; }
        public Builder bloodGroup(String v) { this.bloodGroup = v; return this; }
        public Builder phone(String v) { this.phone = v; return this; }
        public Builder email(String v) { this.email = v; return this; }
        public Builder address(String v) { this.address = v; return this; }
        public Builder allergiesSummary(String v) { this.allergiesSummary = v; return this; }
        public Builder chronicConditions(String v) { this.chronicConditions = v; return this; }

        public Patient build() {
            Objects.requireNonNull(mrn, "mrn is mandatory");
            Objects.requireNonNull(firstName, "firstName is mandatory");
            Objects.requireNonNull(lastName, "lastName is mandatory");
            Objects.requireNonNull(dob, "dob is mandatory");
            Objects.requireNonNull(gender, "gender is mandatory");
            Objects.requireNonNull(phone, "phone is mandatory");
            if (dob.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("dob cannot be in the future");
            }
            return new Patient(this);
        }
    }

    // -- intention-revealing mutation methods (no generic setters) --

    public void updateContactDetails(String phone, String email, String address) {
        if (phone != null) this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public void updateClinicalSummary(String bloodGroup, String allergiesSummary, String chronicConditions) {
        this.bloodGroup = bloodGroup;
        this.allergiesSummary = allergiesSummary;
        this.chronicConditions = chronicConditions;
    }

    public void linkUser(Long userId) {
        this.userId = userId;
    }

    public void markDeleted() {
        this.deleted = true;
    }

    public void addAllergy(PatientAllergy allergy) {
        allergies.add(allergy);
        allergy.assignTo(this);
    }

    public void addEmergencyContact(EmergencyContact contact) {
        emergencyContacts.add(contact);
        contact.assignTo(this);
    }

    // -- getters only --

    public Long getId() { return id; }
    public String getMrn() { return mrn; }
    public Long getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDob() { return dob; }
    public String getGender() { return gender; }
    public String getBloodGroup() { return bloodGroup; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getAllergiesSummary() { return allergiesSummary; }
    public String getChronicConditions() { return chronicConditions; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isDeleted() { return deleted; }
    public List<PatientAllergy> getAllergies() { return allergies; }
    public List<EmergencyContact> getEmergencyContacts() { return emergencyContacts; }
}
