package com.hms.lab.service;

import com.hms.common.event.EventPublisher;
import com.hms.common.exception.BusinessRuleException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.lab.dto.LabOrderRequest;
import com.hms.lab.dto.ResultUploadRequest;
import com.hms.lab.entity.LabOrder;
import com.hms.lab.entity.LabOrderItem;
import com.hms.lab.entity.LabOrderStatus;
import com.hms.lab.entity.LabResult;
import com.hms.lab.entity.TestCatalogue;
import com.hms.lab.factory.TestProcessor;
import com.hms.lab.factory.TestProcessorFactory;
import com.hms.lab.repository.LabOrderItemRepository;
import com.hms.lab.repository.LabOrderRepository;
import com.hms.lab.repository.LabResultRepository;
import com.hms.lab.repository.TestCatalogueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final LabOrderItemRepository labOrderItemRepository;
    private final LabResultRepository labResultRepository;
    private final TestCatalogueRepository testCatalogueRepository;
    private final TestProcessorFactory testProcessorFactory;
    private final EventPublisher eventPublisher;

    public LabOrderService(LabOrderRepository labOrderRepository,
                            LabOrderItemRepository labOrderItemRepository,
                            LabResultRepository labResultRepository,
                            TestCatalogueRepository testCatalogueRepository,
                            TestProcessorFactory testProcessorFactory,
                            EventPublisher eventPublisher) {
        this.labOrderRepository = labOrderRepository;
        this.labOrderItemRepository = labOrderItemRepository;
        this.labResultRepository = labResultRepository;
        this.testCatalogueRepository = testCatalogueRepository;
        this.testProcessorFactory = testProcessorFactory;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public LabOrder createOrder(LabOrderRequest request) {
        for (Long testId : request.testIds()) {
            testCatalogueRepository.findById(testId)
                    .orElseThrow(() -> new ResourceNotFoundException("Test not found in catalogue: " + testId));
        }
        LabOrder.Builder builder = LabOrder.builder()
                .patientId(request.patientId())
                .doctorId(request.doctorId())
                .fastingRequired(request.fastingRequired())
                .testIds(request.testIds());
        if (request.priority() != null) {
            builder.priority(request.priority());
        }
        return labOrderRepository.save(builder.build());
    }

    @Transactional(readOnly = true)
    public LabOrder getById(Long id) {
        return labOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order not found: " + id));
    }

    @Transactional
    public LabOrder updateStatus(Long id, LabOrderStatus status) {
        LabOrder order = getById(id);
        order.markStatus(status);
        return labOrderRepository.save(order);
    }

    @Transactional
    public LabResult uploadResult(Long orderId, ResultUploadRequest request, Long reportedBy) {
        LabOrder order = getById(orderId);
        LabOrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(request.orderItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + request.orderItemId()));

        TestCatalogue test = testCatalogueRepository.findById(item.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Test not found: " + item.getTestId()));

        if (item.getStatus() == LabOrderStatus.COMPLETED) {
            throw new BusinessRuleException("Result already uploaded for this order item");
        }

        TestProcessor processor = testProcessorFactory.resolve(test.getSampleType());
        LabResult result = processor.processResult(item, request, reportedBy);
        result = labResultRepository.save(result);

        item.markStatus(LabOrderStatus.COMPLETED);
        labOrderItemRepository.save(item);
        order.refreshStatusFromItems();
        labOrderRepository.save(order);

        eventPublisher.publish("lab.result.ready", Map.of(
                "orderId", order.getId(),
                "orderItemId", item.getId(),
                "patientId", order.getPatientId(),
                "testCode", test.getCode(),
                "abnormalFlag", result.isAbnormalFlag()
        ));

        return result;
    }
}
