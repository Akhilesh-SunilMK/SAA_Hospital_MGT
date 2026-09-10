CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mrn VARCHAR(30) NOT NULL,
    user_id BIGINT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    blood_group VARCHAR(5) NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150) NULL,
    address VARCHAR(500) NULL,
    allergies_summary VARCHAR(500) NULL,
    chronic_conditions VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_patients_mrn UNIQUE (mrn)
) ENGINE=InnoDB;

CREATE INDEX idx_patients_phone ON patients (phone);
CREATE INDEX idx_patients_name ON patients (last_name, first_name);

CREATE TABLE patient_allergies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    allergen VARCHAR(150) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    noted_on DATE NOT NULL,
    CONSTRAINT fk_allergy_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
) ENGINE=InnoDB;

CREATE TABLE emergency_contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    relation VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    CONSTRAINT fk_contact_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
) ENGINE=InnoDB;

CREATE TABLE admissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    ward_id VARCHAR(50) NOT NULL,
    bed_no VARCHAR(20) NOT NULL,
    admitted_at DATETIME NOT NULL,
    discharged_at DATETIME NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_admission_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
) ENGINE=InnoDB;

CREATE INDEX idx_admissions_patient_status ON admissions (patient_id, status);
