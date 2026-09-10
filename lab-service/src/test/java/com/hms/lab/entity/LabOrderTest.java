package com.hms.lab.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LabOrderTest {

    @Test
    void buildsWithMandatoryFieldsAndDefaults() {
        LabOrder order = LabOrder.builder()
                .patientId(1L)
                .doctorId(2L)
                .addTest(10L)
                .build();

        assertThat(order.getPriority()).isEqualTo(LabPriority.NORMAL);
        assertThat(order.isFastingRequired()).isFalse();
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(LabOrderStatus.ORDERED);
    }

    @Test
    void statPriorityAndFastingAreHonoured() {
        LabOrder order = LabOrder.builder()
                .patientId(1L).doctorId(2L)
                .priority(LabPriority.STAT)
                .fastingRequired(true)
                .testIds(List.of(10L, 11L))
                .build();

        assertThat(order.getPriority()).isEqualTo(LabPriority.STAT);
        assertThat(order.isFastingRequired()).isTrue();
        assertThat(order.getItems()).hasSize(2);
    }

    @Test
    void emptyTestListRejected() {
        assertThatThrownBy(() -> LabOrder.builder().patientId(1L).doctorId(2L).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingPatientIdRejected() {
        assertThatThrownBy(() -> LabOrder.builder().doctorId(2L).addTest(10L).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("patientId");
    }

    @Test
    void noPublicSettersExist() {
        boolean hasSetter = Arrays.stream(LabOrder.class.getMethods())
                .anyMatch(m -> m.getName().startsWith("set") && Modifier.isPublic(m.getModifiers()));
        assertThat(hasSetter).isFalse();
    }

    @Test
    void validStatusTransitionSucceeds() {
        LabOrder order = LabOrder.builder().patientId(1L).doctorId(2L).addTest(10L).build();
        order.markStatus(LabOrderStatus.COLLECTED);
        assertThat(order.getStatus()).isEqualTo(LabOrderStatus.COLLECTED);
    }

    @Test
    void skippingAStageIsRejected() {
        LabOrder order = LabOrder.builder().patientId(1L).doctorId(2L).addTest(10L).build();
        assertThatThrownBy(() -> order.markStatus(LabOrderStatus.COMPLETED))
                .isInstanceOf(com.hms.common.exception.BusinessRuleException.class);
    }

    @Test
    void cannotTransitionOutOfTerminalState() {
        LabOrder order = LabOrder.builder().patientId(1L).doctorId(2L).addTest(10L).build();
        order.markStatus(LabOrderStatus.CANCELLED);
        assertThatThrownBy(() -> order.markStatus(LabOrderStatus.COLLECTED))
                .isInstanceOf(com.hms.common.exception.BusinessRuleException.class);
    }
}
