package com.hms.pharmacy.service;

import com.hms.common.event.EventPublisher;
import com.hms.common.exception.BusinessRuleException;
import com.hms.pharmacy.dto.DispenseRequest;
import com.hms.pharmacy.entity.Drug;
import com.hms.pharmacy.entity.DispenseOrder;
import com.hms.pharmacy.entity.StockBatch;
import com.hms.pharmacy.repository.DispenseOrderRepository;
import com.hms.pharmacy.repository.DrugRepository;
import com.hms.pharmacy.repository.StockBatchRepository;
import com.hms.pharmacy.strategy.PatientCategory;
import com.hms.pharmacy.strategy.PricingStrategyFactory;
import com.hms.pharmacy.strategy.RetailPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DispenseServiceTest {

    private DrugRepository drugRepository;
    private StockBatchRepository stockBatchRepository;
    private DispenseOrderRepository dispenseOrderRepository;
    private EventPublisher eventPublisher;
    private DispenseService dispenseService;

    @BeforeEach
    void setUp() {
        drugRepository = mock(DrugRepository.class);
        stockBatchRepository = mock(StockBatchRepository.class);
        dispenseOrderRepository = mock(DispenseOrderRepository.class);
        eventPublisher = mock(EventPublisher.class);
        PricingStrategyFactory pricingStrategyFactory =
                new PricingStrategyFactory(List.of(new RetailPricingStrategy()));
        dispenseService = new DispenseService(drugRepository, stockBatchRepository, dispenseOrderRepository,
                pricingStrategyFactory, eventPublisher);

        when(dispenseOrderRepository.save(any(DispenseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockBatchRepository.save(any(StockBatch.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void rejectsDispenseWhenNoValidBatch() {
        Drug drug = new Drug("D1", "Paracetamol", null, "TABLET", "500mg", null, BigDecimal.TEN, 10);
        when(drugRepository.findById(1L)).thenReturn(Optional.of(drug));
        when(stockBatchRepository.findByDrugIdAndQuantityGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                eq(1L), eq(0), any(LocalDate.class))).thenReturn(List.of());

        DispenseRequest req = new DispenseRequest(100L, null, PatientCategory.GENERAL,
                List.of(new DispenseRequest.Item(1L, 2)));

        assertThatThrownBy(() -> dispenseService.dispense(req, 5L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void decrementsBatchAndFiresLowStockEventWhenBelowReorderLevel() {
        Drug drug = new Drug("D1", "Paracetamol", null, "TABLET", "500mg", null, BigDecimal.TEN, 10);
        setId(drug, 1L);
        StockBatch batch = new StockBatch(1L, "B1", 12, LocalDate.now().plusMonths(6));
        setId(batch, 100L);

        when(drugRepository.findById(1L)).thenReturn(Optional.of(drug));
        when(stockBatchRepository.findByDrugIdAndQuantityGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                eq(1L), eq(0), any(LocalDate.class))).thenReturn(List.of(batch));
        when(stockBatchRepository.findByDrugId(1L)).thenReturn(List.of(batch));

        DispenseRequest req = new DispenseRequest(100L, null, PatientCategory.GENERAL,
                List.of(new DispenseRequest.Item(1L, 10)));

        dispenseService.dispense(req, 5L);

        assertThat(batch.getQuantity()).isEqualTo(2);

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventPublisher).publish(eq("stock.below.threshold"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).containsEntry("currentQuantity", 2);
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
