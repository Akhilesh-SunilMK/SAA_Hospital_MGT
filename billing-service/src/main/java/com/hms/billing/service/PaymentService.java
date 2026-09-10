package com.hms.billing.service;

import com.hms.billing.dto.PaymentRequestDto;
import com.hms.billing.dto.PaymentResponse;
import com.hms.billing.entity.Invoice;
import com.hms.billing.entity.Payment;
import com.hms.billing.factory.PaymentProcessor;
import com.hms.billing.factory.PaymentProcessorFactory;
import com.hms.billing.factory.PaymentRequest;
import com.hms.billing.factory.PaymentResult;
import com.hms.billing.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentProcessorFactory paymentProcessorFactory;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;

    public PaymentService(PaymentProcessorFactory paymentProcessorFactory, PaymentRepository paymentRepository,
                           InvoiceService invoiceService) {
        this.paymentProcessorFactory = paymentProcessorFactory;
        this.paymentRepository = paymentRepository;
        this.invoiceService = invoiceService;
    }

    @Transactional
    public PaymentResponse record(PaymentRequestDto req) {
        Invoice invoice = invoiceService.findOrThrow(req.invoiceId());

        PaymentProcessor processor = paymentProcessorFactory.getProcessor(req.method());
        PaymentResult result = processor.process(new PaymentRequest(req.invoiceId(), req.method(),
                req.amount(), req.reference()));

        invoice.applyPayment(req.amount());

        Payment payment = new Payment(invoice.getId(), req.method().name(), req.amount(),
                result.transactionRef(), result.success() ? "SUCCESS" : "FAILED");
        paymentRepository.save(payment);

        return PaymentResponse.of(payment, invoice.getStatus(), invoice.getBalance());
    }
}
