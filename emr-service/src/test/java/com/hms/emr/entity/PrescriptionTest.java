package com.hms.emr.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrescriptionTest {

    @Test
    void buildsWithAtLeastOneItem() {
        Prescription p = Prescription.builder()
                .recordId(1L).patientId(2L).doctorId(3L)
                .addItem(new PrescriptionItem("Paracetamol", "500mg", "TDS", 5, "After food"))
                .build();

        assertThat(p.getItems()).hasSize(1);
        assertThat(p.getStatus()).isEqualTo(PrescriptionStatus.ISSUED);
    }

    @Test
    void emptyItemsRejected() {
        assertThatThrownBy(() -> Prescription.builder().recordId(1L).patientId(2L).doctorId(3L).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingRecordIdRejected() {
        assertThatThrownBy(() -> Prescription.builder().patientId(2L).doctorId(3L)
                .addItem(new PrescriptionItem("X", "1mg", "OD", 1, null))
                .build())
                .isInstanceOf(NullPointerException.class);
    }
}
