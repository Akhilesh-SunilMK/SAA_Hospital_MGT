package com.hms.webui.security;

import com.hms.common.security.Role;

import java.io.Serializable;

/**
 * What we keep in the HTTP session after login. For PATIENT-role users, {@link #patientId()} is the
 * patient-service profile linked to this login — a different id from {@link #userId()} — filled in
 * by {@link CurrentUserArgumentResolver} once reception has registered the profile.
 */
public class SessionUser implements Serializable {

    private final Long userId;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final Role role;
    private String accessToken;
    private String refreshToken;
    private Long patientId;

    public SessionUser(Long userId, String username, String email, String firstName, String lastName,
                        Role role, String accessToken, String refreshToken) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Long userId() { return userId; }
    public String username() { return username; }
    public String email() { return email; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String fullName() { return firstName + " " + lastName; }
    public Role role() { return role; }
    public String accessToken() { return accessToken; }
    public String refreshToken() { return refreshToken; }

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    /** The linked patient profile id for PATIENT users; null until one is linked (and for staff). */
    public Long patientId() { return patientId; }

    public void linkPatient(Long patientId) { this.patientId = patientId; }

    public boolean hasAnyRole(Role... roles) {
        for (Role r : roles) {
            if (r == role) return true;
        }
        return false;
    }

    public boolean isPatient() { return role == Role.PATIENT; }
    public boolean isDoctor() { return role == Role.DOCTOR; }
    public boolean isNurse() { return role == Role.NURSE; }
    public boolean isReceptionist() { return role == Role.RECEPTIONIST; }
    public boolean isLabTech() { return role == Role.LAB_TECH; }
    public boolean isPharmacist() { return role == Role.PHARMACIST; }
    public boolean isAccountant() { return role == Role.ACCOUNTANT; }
    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isStaff() { return role != Role.PATIENT; }
}
