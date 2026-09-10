package com.hms.lab.factory;

import com.hms.lab.dto.ResultUploadRequest;
import com.hms.lab.entity.LabOrderItem;
import com.hms.lab.entity.LabResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestProcessorFactoryTest {

    private final TestProcessorFactory factory = new TestProcessorFactory(
            List.of(new BloodTestProcessor(), new UrineTestProcessor(), new ImagingTestProcessor(), new GenericTestProcessor()));

    @Test
    void resolvesBloodProcessor() {
        assertThat(factory.resolve("BLOOD")).isInstanceOf(BloodTestProcessor.class);
    }

    @Test
    void resolvesCaseInsensitively() {
        assertThat(factory.resolve("blood")).isInstanceOf(BloodTestProcessor.class);
    }

    @Test
    void unrecognisedSampleTypeFallsBackToGeneric() {
        assertThat(factory.resolve("SALIVA")).isInstanceOf(GenericTestProcessor.class);
    }

    @Test
    void nullSampleTypeRejected() {
        assertThatThrownBy(() -> factory.resolve(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankSampleTypeRejected() {
        assertThatThrownBy(() -> factory.resolve("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bloodProcessorComputesAbnormalFromRange() {
        LabOrderItem item = new LabOrderItem(1L);
        ResultUploadRequest request = new ResultUploadRequest(1L, "15.5", "g/dL", "12.0-15.0", null);
        LabResult result = factory.resolve("BLOOD").processResult(item, request, 99L);
        assertThat(result.isAbnormalFlag()).isTrue();
    }

    @Test
    void bloodProcessorInRangeIsNormal() {
        LabOrderItem item = new LabOrderItem(1L);
        ResultUploadRequest request = new ResultUploadRequest(1L, "13.0", "g/dL", "12.0-15.0", null);
        LabResult result = factory.resolve("BLOOD").processResult(item, request, 99L);
        assertThat(result.isAbnormalFlag()).isFalse();
    }
}
