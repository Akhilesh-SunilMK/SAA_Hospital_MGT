CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    registration_no VARCHAR(50) NOT NULL,
    qualification VARCHAR(200) NOT NULL,
    specialisation VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    CONSTRAINT uk_doctors_registration_no UNIQUE (registration_no)
) ENGINE=InnoDB;

CREATE INDEX idx_doctors_specialisation ON doctors (specialisation);
CREATE INDEX idx_doctors_department ON doctors (department);

CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_min INT NOT NULL DEFAULT 15,
    CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id)
) ENGINE=InnoDB;

CREATE INDEX idx_schedules_doctor_day ON schedules (doctor_id, day_of_week);

CREATE TABLE leaves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason VARCHAR(255) NULL,
    CONSTRAINT fk_leave_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id)
) ENGINE=InnoDB;
