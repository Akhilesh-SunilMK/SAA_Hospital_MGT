package com.hms.webui.service;

import com.hms.common.dto.PageResponse;
import com.hms.webui.client.ApiClient;
import com.hms.webui.dto.AppointmentDtos.*;
import com.hms.webui.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final ApiClient api;

    public AppointmentService(ApiClient api) {
        this.api = api;
    }

    public AppointmentResponse book(BookAppointmentRequest request, SessionUser user) {
        return api.post("/api/v1/appointments", request, AppointmentResponse.class, user);
    }

    public AppointmentResponse get(Long id, SessionUser user) {
        return api.get("/api/v1/appointments/" + id, AppointmentResponse.class, user);
    }

    public PageResponse<AppointmentResponse> search(Long patientId, Long doctorId, LocalDate date, String status,
                                                      int page, int size, SessionUser user) {
        String uri = UriComponentsBuilder.fromPath("/api/v1/appointments")
                .queryParamIfPresent("patientId", Optional.ofNullable(patientId))
                .queryParamIfPresent("doctorId", Optional.ofNullable(doctorId))
                .queryParamIfPresent("date", Optional.ofNullable(date))
                .queryParamIfPresent("status", Optional.ofNullable(status).filter(s -> !s.isBlank()))
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
        return api.get(uri, api.pageType(AppointmentResponse.class), user);
    }

    public AppointmentResponse reschedule(Long id, RescheduleRequest request, SessionUser user) {
        return api.patch("/api/v1/appointments/" + id + "/reschedule", request, AppointmentResponse.class, user);
    }

    public void cancel(Long id, SessionUser user) {
        api.patch("/api/v1/appointments/" + id + "/cancel", null, (Class<Void>) null, user);
    }

    public AppointmentResponse updateStatus(Long id, StatusUpdateRequest request, SessionUser user) {
        return api.patch("/api/v1/appointments/" + id + "/status", request, AppointmentResponse.class, user);
    }

    public List<AppointmentResponse> queue(Long doctorId, SessionUser user) {
        String uri = UriComponentsBuilder.fromPath("/api/v1/appointments/queue")
                .queryParam("doctorId", doctorId)
                .toUriString();
        AppointmentResponse[] arr = api.get(uri, AppointmentResponse[].class, user);
        return arr == null ? List.of() : List.of(arr);
    }
}
