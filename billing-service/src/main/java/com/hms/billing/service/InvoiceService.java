package com.hms.billing.service;

import com.hms.common.event.EventPublisher;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.billing.dto.InvoiceRequest;
import com.hms.billing.dto.InvoiceResponse;
import com.hms.billing.entity.Invoice;
import com.hms.billing.entity.InvoiceStatus;
import com.hms.billing.repository.InvoiceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Map;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final EventPublisher eventPublisher;

    public InvoiceService(InvoiceRepository invoiceRepository, EventPublisher eventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public InvoiceResponse create(InvoiceRequest req) {
        Invoice.Builder builder = Invoice.builder()
                .invoiceNo(generateInvoiceNo())
                .patientId(req.patientId())
                .appointmentId(req.appointmentId())
                .discount(req.discount());

        req.resolvedItems().forEach(item -> builder.addItem(
                item.description(), item.category(),
                item.quantity() != null ? item.quantity() : 1,
                item.unitPrice(), item.taxRate()));

        Invoice invoice;
        try {
            invoice = invoiceRepository.save(builder.build());
        } catch (DataIntegrityViolationException e) {
            // extremely unlikely invoice_no collision under the sequential scheme below; retry once
            invoice = invoiceRepository.save(Invoice.builder()
                    .invoiceNo(generateInvoiceNo())
                    .patientId(req.patientId())
                    .appointmentId(req.appointmentId())
                    .discount(req.discount())
                    .build());
        }

        eventPublisher.publish("invoice.generated", Map.of(
                "invoiceId", invoice.getId(),
                "invoiceNo", invoice.getInvoiceNo(),
                "patientId", invoice.getPatientId(),
                "amount", invoice.getTotal()
        ));

        return InvoiceResponse.from(invoice);
    }

    public InvoiceResponse getById(Long id) {
        return InvoiceResponse.from(findOrThrow(id));
    }

    public Invoice findOrThrow(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    public Page<InvoiceResponse> query(Long patientId, InvoiceStatus status, Pageable pageable) {
        Page<Invoice> page;
        if (patientId != null && status != null) {
            page = invoiceRepository.findByPatientIdAndStatus(patientId, status, pageable);
        } else if (patientId != null) {
            page = invoiceRepository.findByPatientId(patientId, pageable);
        } else {
            page = invoiceRepository.findAll(pageable);
        }
        return page.map(InvoiceResponse::from);
    }

    private String generateInvoiceNo() {
        String prefix = "INV-" + Year.now().getValue() + "-";
        long seq = invoiceRepository.countByInvoiceNoStartingWith(prefix) + 1;
        return prefix + String.format("%05d", seq);
    }
}
