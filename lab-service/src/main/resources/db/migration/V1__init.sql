-- hms_lab schema (SRS 8.1). `fasting_required` on lab_orders is an additive column (not in the
-- original SRS table list) backing the LabTestOrder Builder's "fasting requirement" attribute
-- (SRS 4.3.2).

CREATE TABLE test_catalogue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    sample_type VARCHAR(30) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    turnaround_hours INT NOT NULL
);

CREATE TABLE lab_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    ordered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    priority VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    fasting_required BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE lab_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    test_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES lab_orders (id)
);

CREATE TABLE lab_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id BIGINT NOT NULL,
    value VARCHAR(255) NOT NULL,
    unit VARCHAR(30) NULL,
    reference_range VARCHAR(100) NULL,
    abnormal_flag BOOLEAN NOT NULL DEFAULT FALSE,
    reported_by BIGINT NOT NULL,
    reported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_item FOREIGN KEY (order_item_id) REFERENCES lab_order_items (id)
);
