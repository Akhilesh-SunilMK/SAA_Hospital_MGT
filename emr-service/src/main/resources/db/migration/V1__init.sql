-- hms_emr schema (SRS 8.1). `record_type` on medical_records is an additive column (not in the
-- original SRS table list) backing the RecordFactory Factory Method pattern (SRS 4.1 idiom
-- applied to emr-service) so the seeded record kind is queryable/persisted, not just used at
-- creation time.

CREATE TABLE medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_id BIGINT NULL,
    record_type VARCHAR(20) NOT NULL DEFAULT 'CONSULTATION',
    chief_complaint VARCHAR(500) NULL,
    notes TEXT NULL,
    finalised BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_medical_records_patient_created ON medical_records (patient_id, created_at DESC);

CREATE TABLE record_amendments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    amended_by BIGINT NOT NULL,
    previous_value TEXT NOT NULL,
    new_value TEXT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    amended_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_amendment_record FOREIGN KEY (record_id) REFERENCES medical_records (id)
);

CREATE TABLE diagnoses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    icd10_code VARCHAR(10) NOT NULL,
    description VARCHAR(255) NOT NULL,
    type VARCHAR(30) NULL,
    CONSTRAINT fk_diagnosis_record FOREIGN KEY (record_id) REFERENCES medical_records (id)
);

CREATE TABLE vitals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    record_id BIGINT NULL,
    bp_systolic INT NULL,
    bp_diastolic INT NULL,
    pulse INT NULL,
    temperature DECIMAL(4,1) NULL,
    spo2 INT NULL,
    height_cm DECIMAL(5,1) NULL,
    weight_kg DECIMAL(5,1) NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vitals_record ON vitals (record_id);

CREATE TABLE prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX idx_prescriptions_patient ON prescriptions (patient_id);

CREATE TABLE prescription_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    drug_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    duration_days INT NOT NULL,
    instructions VARCHAR(255) NULL,
    CONSTRAINT fk_item_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions (id)
);

CREATE TABLE emr_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    ip_address VARCHAR(45) NULL,
    accessed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
