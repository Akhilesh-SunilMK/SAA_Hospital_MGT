package com.hms.patient.dto;

import com.hms.patient.entity.EmergencyContact;

public record EmergencyContactResponse(Long id, String name, String relation, String phone) {
    public static EmergencyContactResponse from(EmergencyContact c) {
        return new EmergencyContactResponse(c.getId(), c.getName(), c.getRelation(), c.getPhone());
    }
}
