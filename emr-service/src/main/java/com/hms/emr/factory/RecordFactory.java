package com.hms.emr.factory;

import com.hms.emr.dto.CreateRecordRequest;
import com.hms.emr.entity.MedicalRecord;
import com.hms.emr.entity.RecordType;

/**
 * Factory Method pattern (traceability matrix: emr-service -> RecordFactory).
 * Each {@link RecordType} needs different default seeding of the MedicalRecord.Builder before
 * the caller fills in the request-specific fields — e.g. an emergency record gets a distinct
 * chief-complaint template. Adding a new RecordType requires only a new {@code @Component}
 * implementing this interface; {@link RecordFactoryRegistry} picks it up automatically.
 */
public interface RecordFactory {

    RecordType getType();

    MedicalRecord.Builder newBuilder(CreateRecordRequest request);
}
