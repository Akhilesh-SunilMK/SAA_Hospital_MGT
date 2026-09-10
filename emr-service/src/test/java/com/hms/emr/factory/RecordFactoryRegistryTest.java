package com.hms.emr.factory;

import com.hms.emr.entity.RecordType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordFactoryRegistryTest {

    private final RecordFactoryRegistry registry = new RecordFactoryRegistry(
            List.of(new ConsultationRecordFactory(), new FollowUpRecordFactory(), new EmergencyRecordFactory()));

    @Test
    void resolvesConsultationFactory() {
        assertThat(registry.resolve(RecordType.CONSULTATION)).isInstanceOf(ConsultationRecordFactory.class);
    }

    @Test
    void resolvesEmergencyFactory() {
        assertThat(registry.resolve(RecordType.EMERGENCY)).isInstanceOf(EmergencyRecordFactory.class);
    }

    @Test
    void unknownTypeThrows() {
        assertThatThrownBy(() -> registry.resolve(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addingNewFactoryRequiresNoRegistryChange() {
        RecordFactory customFactory = new RecordFactory() {
            @Override
            public RecordType getType() {
                return RecordType.FOLLOW_UP; // reusing an existing type slot for the test
            }

            @Override
            public com.hms.emr.entity.MedicalRecord.Builder newBuilder(com.hms.emr.dto.CreateRecordRequest request) {
                return com.hms.emr.entity.MedicalRecord.builder().patientId(1L).doctorId(1L);
            }
        };
        RecordFactoryRegistry customRegistry = new RecordFactoryRegistry(List.of(customFactory));
        assertThat(customRegistry.resolve(RecordType.FOLLOW_UP)).isSameAs(customFactory);
    }
}
