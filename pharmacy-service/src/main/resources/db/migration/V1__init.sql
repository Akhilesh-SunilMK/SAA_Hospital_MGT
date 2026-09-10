CREATE TABLE drugs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    generic_name VARCHAR(150) NOT NULL,
    brand_name VARCHAR(150) NULL,
    form VARCHAR(30) NOT NULL,
    strength VARCHAR(30) NOT NULL,
    manufacturer VARCHAR(150) NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    reorder_level INT NOT NULL DEFAULT 10
);

CREATE TABLE stock_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    expiry_date DATE NOT NULL,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_stock_batches_drug FOREIGN KEY (drug_id) REFERENCES drugs(id)
);
CREATE INDEX idx_stock_batches_drug_expiry ON stock_batches (drug_id, expiry_date);

CREATE TABLE dispense_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NULL,
    patient_id BIGINT NOT NULL,
    dispensed_by BIGINT NOT NULL,
    dispensed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL
);

CREATE TABLE dispense_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispense_order_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_dispense_items_order FOREIGN KEY (dispense_order_id) REFERENCES dispense_orders(id)
);
