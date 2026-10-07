package com.hms.webui.service;

import com.hms.common.dto.PageResponse;
import com.hms.webui.client.ApiClient;
import com.hms.webui.dto.PatientDtos.*;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class PatientService {

    private final ApiClient api;

    public PatientService(ApiClient api) {
        this.api = api;
    }

    public PatientResponse create(PatientRequest request, SessionUser user) {
        return api.post("/api/v1/patients", request, PatientResponse.class, user);
    }

    /** The calling PATIENT's own profile id, or null if reception hasn't linked one to their login yet. */
    public Long findMyPatientId(SessionUser user) {
        try {
            PatientResponse me = api.get("/api/v1/patients/me", PatientResponse.class, user);
            return me == null ? null : me.id();
        } catch (ApiException e) {
            if (e.statusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    public PatientResponse get(Long id, SessionUser user) {
        return api.get("/api/v1/patients/" + id, PatientResponse.class, user);
    }

    public PageResponse<PatientResponse> search(String search, int page, int size, SessionUser user) {
        String uri = UriComponentsBuilder.fromPath("/api/v1/patients")
                .queryParamIfPresent("search", java.util.Optional.ofNullable(search).filter(s -> !s.isBlank()))
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
        return api.get(uri, api.pageType(PatientResponse.class), user);
    }

    public PatientResponse update(Long id, PatientUpdateRequest request, SessionUser user) {
        return api.put("/api/v1/patients/" + id, request, PatientResponse.class, user);
    }

    public void delete(Long id, SessionUser user) {
        api.delete("/api/v1/patients/" + id, user);
    }

    public AdmissionResponse admit(Long id, AdmitRequest request, SessionUser user) {
        return api.post("/api/v1/patients/" + id + "/admit", request, AdmissionResponse.class, user);
    }

    public AdmissionResponse discharge(Long id, SessionUser user) {
        return api.post("/api/v1/patients/" + id + "/discharge", null, AdmissionResponse.class, user);
    }

    public AllergyResponse addAllergy(Long id, AllergyRequest request, SessionUser user) {
        return api.post("/api/v1/patients/" + id + "/allergies", request, AllergyResponse.class, user);
    }

    public List<AllergyResponse> allergies(Long id, SessionUser user) {
        AllergyResponse[] arr = api.get("/api/v1/patients/" + id + "/allergies", AllergyResponse[].class, user);
        return arr == null ? List.of() : List.of(arr);
    }

    public EmergencyContactResponse addEmergencyContact(Long id, EmergencyContactRequest request, SessionUser user) {
        return api.post("/api/v1/patients/" + id + "/emergency-contacts", request, EmergencyContactResponse.class, user);
    }

    public List<EmergencyContactResponse> emergencyContacts(Long id, SessionUser user) {
        EmergencyContactResponse[] arr = api.get("/api/v1/patients/" + id + "/emergency-contacts", EmergencyContactResponse[].class, user);
        return arr == null ? List.of() : List.of(arr);
    }
}
