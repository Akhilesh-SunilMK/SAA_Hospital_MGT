INSERT INTO drugs (code, generic_name, brand_name, form, strength, manufacturer, unit_price, reorder_level) VALUES
('DRG-001', 'Paracetamol', 'Calpol', 'TABLET', '500mg', 'GSK', 2.50, 50),
('DRG-002', 'Amoxicillin', 'Novamox', 'CAPSULE', '250mg', 'Cipla', 6.00, 30),
('DRG-003', 'Cetirizine', 'Zyrtec', 'TABLET', '10mg', 'UCB', 3.20, 40),
('DRG-004', 'Metformin', 'Glucophage', 'TABLET', '500mg', 'Merck', 4.10, 25),
('DRG-005', 'Insulin Glargine', 'Lantus', 'INJECTION', '100IU/ml', 'Sanofi', 350.00, 10);

INSERT INTO stock_batches (drug_id, batch_no, quantity, expiry_date) VALUES
(1, 'BATCH-P001', 200, DATE_ADD(CURDATE(), INTERVAL 18 MONTH)),
(2, 'BATCH-A001', 150, DATE_ADD(CURDATE(), INTERVAL 12 MONTH)),
(3, 'BATCH-C001', 100, DATE_ADD(CURDATE(), INTERVAL 24 MONTH)),
(4, 'BATCH-M001', 5, DATE_ADD(CURDATE(), INTERVAL 6 MONTH)),
(5, 'BATCH-I001', 20, DATE_ADD(CURDATE(), INTERVAL 9 MONTH)),
(1, 'BATCH-P000-EXPIRED', 40, DATE_SUB(CURDATE(), INTERVAL 1 MONTH));
