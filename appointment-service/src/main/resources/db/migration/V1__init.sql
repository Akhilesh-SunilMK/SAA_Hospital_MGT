CREATE TABLE appointments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    doctor_id       BIGINT NOT NULL,
    slot            DATETIME NOT NULL,
    type            VARCHAR(20) NOT NULL,
    priority        VARCHAR(10) NOT NULL,
    reason          VARCHAR(500) NULL,
    duration_min    INT NOT NULL DEFAULT 15,
    referred_by     BIGINT NULL,
    status          VARCHAR(20) NOT NULL,
    token_no        VARCHAR(30) NULL,
    insurance_claimed BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointments_doctor_slot ON appointments (doctor_id, slot);
CREATE INDEX idx_appointments_patient_status ON appointments (patient_id, status);
CREATE INDEX idx_appointments_slot ON appointments (slot);

CREATE TABLE slot_reservations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id       BIGINT NOT NULL,
    slot            DATETIME NOT NULL,
    appointment_id  BIGINT NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_slot_reservations_doctor_slot UNIQUE (doctor_id, slot)
);
