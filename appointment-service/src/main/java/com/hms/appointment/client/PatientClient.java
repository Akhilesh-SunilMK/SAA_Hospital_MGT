package com.hms.appointment.client;

import com.hms.appointment.dto.PatientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/** SRS 3.3: Appointment Service -> Patient Service (GET /api/v1/patients/{id} for validation). */
@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/api/v1/patients/{id}")
    PatientResponse getPatient(@PathVariable("id") Long patientId, @RequestHeader("Authorization") String authorization);
}
