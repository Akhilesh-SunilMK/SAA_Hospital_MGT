package com.hms.webui.service;

import com.hms.webui.client.ApiClient;
import com.hms.webui.dto.DoctorDtos.*;
import com.hms.webui.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final ApiClient api;

    public DoctorService(ApiClient api) {
        this.api = api;
    }

    public DoctorResponse create(DoctorRequest request, SessionUser user) {
        return api.post("/api/v1/doctors", request, DoctorResponse.class, user);
    }

    public DoctorResponse get(Long id, SessionUser user) {
        return api.get("/api/v1/doctors/" + id, DoctorResponse.class, user);
    }

    public List<DoctorResponse> search(String specialisation, String department, SessionUser user) {
        String uri = UriComponentsBuilder.fromPath("/api/v1/doctors")
                .queryParamIfPresent("specialisation", Optional.ofNullable(specialisation).filter(s -> !s.isBlank()))
                .queryParamIfPresent("department", Optional.ofNullable(department).filter(s -> !s.isBlank()))
                .toUriString();
        DoctorResponse[] arr = api.get(uri, DoctorResponse[].class, user);
        return arr == null ? List.of() : List.of(arr);
    }

    public AvailabilityResponse availability(Long id, LocalDate date, SessionUser user) {
        String uri = UriComponentsBuilder.fromPath("/api/v1/doctors/" + id + "/availability")
                .queryParam("date", date)
                .toUriString();
        return api.get(uri, AvailabilityResponse.class, user);
    }

    public List<ScheduleResponse> setSchedule(Long id, ScheduleRequest request, SessionUser user) {
        ScheduleResponse[] arr = api.put("/api/v1/doctors/" + id + "/schedule", request, ScheduleResponse[].class, user);
        return arr == null ? List.of() : List.of(arr);
    }

    public LeaveResponse addLeave(Long id, LeaveRequest request, SessionUser user) {
        return api.post("/api/v1/doctors/" + id + "/leave", request, LeaveResponse.class, user);
    }

    /**
     * There's no "find doctor by userId" endpoint, so a logged-in DOCTOR resolves their own
     * doctor profile by scanning the (unpaged) doctor directory for a matching userId.
     */
    public DoctorResponse findMine(SessionUser user) {
        return search(null, null, user).stream()
                .filter(d -> d.userId() != null && d.userId().equals(user.userId()))
                .findFirst()
                .orElse(null);
    }
}
