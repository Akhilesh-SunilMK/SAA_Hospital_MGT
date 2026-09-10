INSERT INTO templates (code, channel, subject, body, active) VALUES
('APPOINTMENT_CONFIRMED', 'EMAIL', 'Your appointment is confirmed',
 'Dear patient, your appointment (token {{tokenNumber}}) is confirmed for {{slot}}.', TRUE),
('APPOINTMENT_CONFIRMED', 'SMS', NULL,
 'Appt confirmed. Token {{tokenNumber}} at {{slot}}.', TRUE),
('INVOICE_GENERATED', 'EMAIL', 'Invoice generated',
 'Invoice {{invoiceNo}} for amount {{amount}} has been generated.', TRUE),
('LAB_RESULT_READY', 'EMAIL', 'Lab result ready',
 'Your lab result for order {{orderId}} is now ready for review.', TRUE),
('STOCK_LOW', 'EMAIL', 'Stock below reorder level',
 'Drug {{drugName}} stock is at {{currentQuantity}}, below reorder level {{reorderLevel}}.', TRUE);
