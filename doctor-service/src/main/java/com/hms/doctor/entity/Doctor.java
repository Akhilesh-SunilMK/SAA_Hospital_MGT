package com.hms.doctor.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** FR-DR-01, FR-DR-06: profile plus consultation fee. Simple entity — no optional-field
 * explosion, so a plain constructor/setters combo is proportionate here (Builder pattern for
 * this service is applied to {@link Schedule} instead, per the pattern-traceability matrix). */
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "registration_no", nullable = false, unique = true, length = 50)
    private String registrationNo;

    @Column(nullable = false, length = 200)
    private String qualification;

    @Column(nullable = false, length = 100)
    private String specialisation;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee;

    protected Doctor() {
    }

    public Doctor(Long userId, String firstName, String lastName, String registrationNo,
                  String qualification, String specialisation, String department, BigDecimal consultationFee) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registrationNo = registrationNo;
        this.qualification = qualification;
        this.specialisation = specialisation;
        this.department = department;
        this.consultationFee = consultationFee;
    }

    public void updateFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRegistrationNo() { return registrationNo; }
    public String getQualification() { return qualification; }
    public String getSpecialisation() { return specialisation; }
    public String getDepartment() { return department; }
    public BigDecimal getConsultationFee() { return consultationFee; }
}
