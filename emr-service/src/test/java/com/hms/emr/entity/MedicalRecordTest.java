package com.hms.emr.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicalRecordTest {

    @Test
    void buildsWithMandatoryFields() {
        MedicalRecord record = MedicalRecord.builder()
                .patientId(1L)
                .doctorId(2L)
                .recordType(RecordType.CONSULTATION)
                .chiefComplaint("Fever")
                .build();

        assertThat(record.getPatientId()).isEqualTo(1L);
        assertThat(record.getDoctorId()).isEqualTo(2L);
        assertThat(record.isFinalised()).isFalse();
    }

    @Test
    void missingPatientIdThrows() {
        assertThatThrownBy(() -> MedicalRecord.builder().doctorId(2L).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("patientId");
    }

    @Test
    void noPublicSettersExist() {
        boolean hasSetter = Arrays.stream(MedicalRecord.class.getMethods())
                .anyMatch(m -> m.getName().startsWith("set") && Modifier.isPublic(m.getModifiers()));
        assertThat(hasSetter).isFalse();
    }

    @Test
    void cannotAmendBeforeFinalisation() {
        MedicalRecord record = MedicalRecord.builder().patientId(1L).doctorId(2L).build();
        assertThatThrownBy(() -> record.amend(new RecordAmendment(9L, "old", "new", "typo fix")))
                .isInstanceOf(com.hms.common.exception.BusinessRuleException.class);
    }

    @Test
    void finaliseThenAmendSucceeds() {
        MedicalRecord record = MedicalRecord.builder().patientId(1L).doctorId(2L).build();
        record.finalise();
        record.amend(new RecordAmendment(9L, "old", "corrected", "typo fix"));
        assertThat(record.getAmendments()).hasSize(1);
        assertThat(record.getNotes()).isEqualTo("corrected");
    }

    @Test
    void cannotUpdateDraftOnceFinalised() {
        MedicalRecord record = MedicalRecord.builder().patientId(1L).doctorId(2L).build();
        record.finalise();
        assertThatThrownBy(() -> record.updateDraft("new complaint", "new notes"))
                .isInstanceOf(com.hms.common.exception.BusinessRuleException.class);
    }
}
