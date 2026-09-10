package com.hms.appointment.client;

import com.hms.appointment.dto.DoctorAvailabilityResponse;
import com.hms.appointment.dto.DoctorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * SRS 3.3: Appointment Service -> Doctor Service (GET /api/v1/doctors/{id}/availability).
 * The date is passed as a pre-formatted ISO-8601 string (see DownstreamIntegrationService)
 * rather than a LocalDate, because Feign/Spring's default argument conversion for LocalDate
 * query params is locale-sensitive (e.g. renders as "9/15/26" under the JVM's default locale)
 * and doctor-service's @DateTimeFormat(iso = DATE) parser rejects anything but ISO format.
 */
@FeignClient(name = "doctor-service")
public interface DoctorClient {

    @GetMapping("/api/v1/doctors/{id}/availability")
    DoctorAvailabilityResponse getAvailability(@PathVariable("id") Long doctorId,
                                                @RequestParam("date") String isoDate,
                                                @RequestHeader("Authorization") String authorization);

    @GetMapping("/api/v1/doctors/{id}")
    DoctorResponse getDoctor(@PathVariable("id") Long doctorId, @RequestHeader("Authorization") String authorization);
}
