package com.hms.pharmacy.service;

import com.hms.common.event.EventPublisher;
import com.hms.common.exception.BusinessRuleException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.pharmacy.dto.DispenseRequest;
import com.hms.pharmacy.dto.DispenseResponse;
import com.hms.pharmacy.entity.Drug;
import com.hms.pharmacy.entity.DispenseOrder;
import com.hms.pharmacy.entity.StockBatch;
import com.hms.pharmacy.repository.DispenseOrderRepository;
import com.hms.pharmacy.repository.DrugRepository;
import com.hms.pharmacy.repository.StockBatchRepository;
import com.hms.pharmacy.strategy.PricingStrategy;
import com.hms.pharmacy.strategy.PricingStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Dispensing must be atomic (FR-PH-03): batch selection, decrement, pricing, and the resulting
 * dispense order are all committed (or rolled back) together in one @Transactional method.
 */
@Service
public class DispenseService {

    private static final Logger log = LoggerFactory.getLogger(DispenseService.class);

    private final DrugRepository drugRepository;
    private final StockBatchRepository stockBatchRepository;
    private final DispenseOrderRepository dispenseOrderRepository;
    private final PricingStrategyFactory pricingStrategyFactory;
    private final EventPublisher eventPublisher;

    public DispenseService(DrugRepository drugRepository, StockBatchRepository stockBatchRepository,
                            DispenseOrderRepository dispenseOrderRepository,
                            PricingStrategyFactory pricingStrategyFactory, EventPublisher eventPublisher) {
        this.drugRepository = drugRepository;
        this.stockBatchRepository = stockBatchRepository;
        this.dispenseOrderRepository = dispenseOrderRepository;
        this.pricingStrategyFactory = pricingStrategyFactory;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DispenseResponse dispense(DispenseRequest req, Long dispensedBy) {
        PricingStrategy pricingStrategy = pricingStrategyFactory.resolve(req.categoryOrDefault());
        DispenseOrder.Builder builder = DispenseOrder.builder()
                .patientId(req.patientId())
                .prescriptionId(req.prescriptionId())
                .dispensedBy(dispensedBy);

        for (DispenseRequest.Item item : req.items()) {
            Drug drug = drugRepository.findById(item.drugId())
                    .orElseThrow(() -> new ResourceNotFoundException("Drug not found: " + item.drugId()));

            List<StockBatch> candidates = stockBatchRepository
                    .findByDrugIdAndQuantityGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                            drug.getId(), 0, LocalDate.now());
            if (candidates.isEmpty()) {
                throw new BusinessRuleException(
                        "No valid (non-expired, in-stock) batch available for drug " + drug.getGenericName());
            }

            int remaining = item.quantity();
            for (StockBatch batch : candidates) {
                if (remaining <= 0) {
                    break;
                }
                int take = Math.min(remaining, batch.getQuantity());
                batch.decrement(take);
                stockBatchRepository.save(batch);
                BigDecimal linePrice = pricingStrategy.price(drug.getUnitPrice(), take)
                        .divide(BigDecimal.valueOf(take), 4, java.math.RoundingMode.HALF_UP);
                builder.addItem(drug.getId(), batch.getId(), take, linePrice);
                remaining -= take;
            }
            if (remaining > 0) {
                throw new BusinessRuleException(
                        "Insufficient stock across all batches for drug " + drug.getGenericName());
            }

            checkLowStock(drug);
        }

        DispenseOrder saved = dispenseOrderRepository.save(builder.build());
        return DispenseResponse.from(saved);
    }

    private void checkLowStock(Drug drug) {
        int total = stockBatchRepository.findByDrugId(drug.getId()).stream()
                .filter(b -> !b.isExpired())
                .mapToInt(StockBatch::getQuantity)
                .sum();
        if (total <= drug.getReorderLevel()) {
            log.info("Stock for drug {} below reorder level ({} <= {})", drug.getGenericName(), total, drug.getReorderLevel());
            eventPublisher.publish("stock.below.threshold", Map.of(
                    "drugId", drug.getId(),
                    "drugName", drug.getGenericName(),
                    "currentQuantity", total,
                    "reorderLevel", drug.getReorderLevel()
            ));
        }
    }
}
