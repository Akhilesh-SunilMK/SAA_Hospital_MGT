package com.hms.billing.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.billing.dto.RefundRequest;
import com.hms.billing.entity.Invoice;
import com.hms.billing.entity.Payment;
import com.hms.billing.entity.Refund;
import com.hms.billing.repository.InvoiceRepository;
import com.hms.billing.repository.PaymentRepository;
import com.hms.billing.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final RefundRepository refundRepository;

    public RefundService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository,
                          RefundRepository refundRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.refundRepository = refundRepository;
    }

    @Transactional
    public Refund issue(RefundRequest req, Long processedBy) {
        Payment payment = paymentRepository.findById(req.paymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + req.paymentId()));
        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + payment.getInvoiceId()));

        // Simplification: a refund against a cancelled service marks the whole invoice REFUNDED
        // (FR-BL-07 requires only the audit reason, not partial-refund invoice-state modelling).
        invoice.markRefunded();

        Refund refund = new Refund(payment.getId(), req.amount(), req.reason(), processedBy);
        return refundRepository.save(refund);
    }
}
