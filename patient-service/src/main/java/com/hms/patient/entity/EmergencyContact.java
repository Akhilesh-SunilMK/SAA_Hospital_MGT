package com.hms.patient.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String relation;

    @Column(nullable = false, length = 20)
    private String phone;

    protected EmergencyContact() {
    }

    public EmergencyContact(String name, String relation, String phone) {
        this.name = name;
        this.relation = relation;
        this.phone = phone;
    }

    void assignTo(Patient patient) {
        this.patient = patient;
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public String getName() { return name; }
    public String getRelation() { return relation; }
    public String getPhone() { return phone; }
}
