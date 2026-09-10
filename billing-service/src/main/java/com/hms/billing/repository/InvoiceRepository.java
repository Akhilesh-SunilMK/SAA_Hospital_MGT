package com.hms.billing.repository;

import com.hms.billing.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Page<Invoice> findByPatientId(Long patientId, Pageable pageable);

    Page<Invoice> findByPatientIdAndStatus(Long patientId, com.hms.billing.entity.InvoiceStatus status, Pageable pageable);

    long countByInvoiceNoStartingWith(String prefix);
}
