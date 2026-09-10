package com.hms.lab.factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resolves the right {@link TestProcessor} by sample type (same registry idiom as
 * NotificationSenderFactory, SRS 4.1.1). A {@code null}/blank sample type is rejected outright;
 * an unrecognised-but-present sample type falls back to {@link GenericTestProcessor} rather than
 * failing outright, since lab systems shouldn't hard-stop on a catalogue entry with a sample
 * type nobody's written a dedicated processor for yet.
 */
@Component
public class TestProcessorFactory {

    private final Map<String, TestProcessor> registry;
    private final TestProcessor genericProcessor;

    public TestProcessorFactory(List<TestProcessor> processors) {
        this.registry = processors.stream()
                .collect(Collectors.toMap(TestProcessor::getSampleType, p -> p));
        this.genericProcessor = registry.getOrDefault(GenericTestProcessor.SAMPLE_TYPE,
                processors.stream().filter(p -> p instanceof GenericTestProcessor).findFirst().orElseThrow());
    }

    public TestProcessor resolve(String sampleType) {
        if (sampleType == null || sampleType.isBlank()) {
            throw new IllegalArgumentException("sampleType must not be null/blank");
        }
        return registry.getOrDefault(sampleType.toUpperCase(), genericProcessor);
    }
}
