package com.hms.pharmacy.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DispenseOrderTest {

    @Test
    void buildsWithComputedTotal() {
        DispenseOrder order = DispenseOrder.builder()
                .patientId(1L)
                .dispensedBy(2L)
                .addItem(10L, 100L, 2, new BigDecimal("5.00"))
                .addItem(11L, 101L, 1, new BigDecimal("3.00"))
                .build();

        assertThat(order.getTotalAmount()).isEqualByComparingTo("13.00");
        assertThat(order.getItems()).hasSize(2);
    }

    @Test
    void requiresAtLeastOneItem() {
        assertThatThrownBy(() -> DispenseOrder.builder().patientId(1L).dispensedBy(2L).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresMandatoryFields() {
        assertThatThrownBy(() -> DispenseOrder.builder()
                .dispensedBy(2L)
                .addItem(10L, 100L, 1, BigDecimal.ONE)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void entityHasNoPublicSetters() {
        boolean hasSetter = Arrays.stream(DispenseOrder.class.getMethods())
                .anyMatch(m -> m.getName().startsWith("set"));
        assertThat(hasSetter).isFalse();
    }
}
