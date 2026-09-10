package com.hms.emr.entity;

/** Differentiates how a MedicalRecord is seeded — see com.hms.emr.factory.RecordFactory. */
public enum RecordType {
    CONSULTATION,
    FOLLOW_UP,
    EMERGENCY
}
