package com.hms.webui.service;

import com.hms.webui.client.ApiClient;
import com.hms.webui.client.DownloadResult;
import com.hms.webui.dto.EmrDtos.*;
import com.hms.webui.security.SessionUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmrService {

    private final ApiClient api;

    public EmrService(ApiClient api) {
        this.api = api;
    }

    public RecordResponse createRecord(CreateRecordRequest request, SessionUser user) {
        return api.post("/api/v1/emr/records", request, RecordResponse.class, user);
    }

    public RecordResponse getRecord(Long id, SessionUser user) {
        return api.get("/api/v1/emr/records/" + id, RecordResponse.class, user);
    }

    public List<RecordResponse> history(Long patientId, SessionUser user) {
        RecordResponse[] arr = api.get("/api/v1/emr/patients/" + patientId + "/history", RecordResponse[].class, user);
        return arr == null ? List.of() : List.of(arr);
    }

    public RecordResponse amend(Long id, AmendRecordRequest request, SessionUser user) {
        return api.post("/api/v1/emr/records/" + id + "/amend", request, RecordResponse.class, user);
    }

    public PrescriptionResponse createPrescription(PrescriptionRequest request, SessionUser user) {
        return api.post("/api/v1/emr/prescriptions", request, PrescriptionResponse.class, user);
    }

    public PrescriptionResponse getPrescription(Long id, SessionUser user) {
        return api.get("/api/v1/emr/prescriptions/" + id, PrescriptionResponse.class, user);
    }

    public VitalsResponse recordVitals(VitalsRequest request, SessionUser user) {
        return api.post("/api/v1/emr/vitals", request, VitalsResponse.class, user);
    }

    public DownloadResult patientSummary(Long patientId, String format, SessionUser user) {
        return api.download("/api/v1/emr/patients/" + patientId + "/summary?format=" + format, user);
    }
}
