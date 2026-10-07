package com.hms.webui.web;

import com.hms.common.security.Role;
import com.hms.webui.client.DownloadResult;
import com.hms.webui.dto.EmrDtos.*;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.Guard;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.DoctorService;
import com.hms.webui.service.EmrService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/emr")
public class EmrWebController {

    private final EmrService emrService;
    private final DoctorService doctorService;

    public EmrWebController(EmrService emrService, DoctorService doctorService) {
        this.emrService = emrService;
        this.doctorService = doctorService;
    }

    /** EMR/prescription requests need the doctor-service doctor id, not the login's userId. */
    private Long requireDoctorId(SessionUser user) {
        var doctor = doctorService.findMine(user);
        if (doctor == null) {
            throw new ApiException(422, "Your login isn't linked to a doctor profile yet. Ask an admin to create one for user ID " + user.userId() + ".", java.util.List.of());
        }
        return doctor.id();
    }

    @GetMapping("/records/new")
    public String newRecordForm(@RequestParam(required = false) Long patientId, @RequestParam(required = false) Long appointmentId,
                                 @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.DOCTOR);
        model.addAttribute("patientId", patientId);
        model.addAttribute("appointmentId", appointmentId);
        return "emr/record-form";
    }

    @PostMapping("/records/new")
    public String createRecord(@CurrentUser SessionUser user,
                                @RequestParam Long patientId, @RequestParam(required = false) Long appointmentId,
                                @RequestParam String recordType, @RequestParam(required = false) String chiefComplaint,
                                @RequestParam(required = false) String notes,
                                @RequestParam(required = false) List<String> icd10Code,
                                @RequestParam(required = false) List<String> diagnosisDescription,
                                @RequestParam(required = false) List<String> diagnosisType,
                                @RequestParam(defaultValue = "false") boolean finalise,
                                RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.DOCTOR);
        List<DiagnosisRequest> diagnoses = new ArrayList<>();
        if (icd10Code != null) {
            for (int i = 0; i < icd10Code.size(); i++) {
                if (icd10Code.get(i) == null || icd10Code.get(i).isBlank()) continue;
                diagnoses.add(new DiagnosisRequest(icd10Code.get(i),
                        diagnosisDescription != null && diagnosisDescription.size() > i ? diagnosisDescription.get(i) : null,
                        diagnosisType != null && diagnosisType.size() > i ? diagnosisType.get(i) : null));
            }
        }
        try {
            Long doctorId = requireDoctorId(user);
            RecordResponse record = emrService.createRecord(new CreateRecordRequest(patientId, doctorId, appointmentId,
                    recordType, chiefComplaint, notes, diagnoses, finalise), user);
            redirectAttributes.addFlashAttribute("success", "EMR record created.");
            return "redirect:/emr/records/" + record.id();
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/emr/records/new?patientId=" + patientId;
        }
    }

    @GetMapping("/records/{id}")
    public String recordDetail(@PathVariable Long id, @CurrentUser SessionUser user, Model model) {
        model.addAttribute("record", emrService.getRecord(id, user));
        return "emr/record-detail";
    }

    @PostMapping("/records/{id}/amend")
    public String amendRecord(@PathVariable Long id, @CurrentUser SessionUser user,
                               @RequestParam String newValue, @RequestParam String reason,
                               RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.DOCTOR);
        try {
            emrService.amend(id, new AmendRecordRequest(newValue, reason), user);
            redirectAttributes.addFlashAttribute("success", "Record amended.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/emr/records/" + id;
    }

    @GetMapping("/patients/{id}/history")
    public String history(@PathVariable Long id, @CurrentUser SessionUser user, Model model) {
        model.addAttribute("patientId", id);
        model.addAttribute("records", emrService.history(id, user));
        return "emr/patient-history";
    }

    @GetMapping("/patients/{id}/summary")
    public ResponseEntity<byte[]> downloadSummary(@PathVariable Long id, @RequestParam(defaultValue = "PDF") String format,
                                                    @CurrentUser SessionUser user) {
        DownloadResult result = emrService.patientSummary(id, format, user);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .body(result.bytes());
    }

    @GetMapping("/prescriptions/new")
    public String newPrescriptionForm(@RequestParam(required = false) Long patientId, @RequestParam(required = false) Long recordId,
                                       @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.DOCTOR);
        model.addAttribute("patientId", patientId);
        model.addAttribute("recordId", recordId);
        return "emr/prescription-form";
    }

    @PostMapping("/prescriptions/new")
    public String createPrescription(@CurrentUser SessionUser user,
                                      @RequestParam Long patientId, @RequestParam(required = false) Long recordId,
                                      @RequestParam List<String> drugName, @RequestParam List<String> dosage,
                                      @RequestParam List<String> frequency, @RequestParam List<Integer> durationDays,
                                      @RequestParam(required = false) List<String> instructions,
                                      RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.DOCTOR);
        List<PrescriptionItemRequest> items = new ArrayList<>();
        for (int i = 0; i < drugName.size(); i++) {
            if (drugName.get(i) == null || drugName.get(i).isBlank()) continue;
            items.add(new PrescriptionItemRequest(drugName.get(i), dosage.get(i), frequency.get(i), durationDays.get(i),
                    instructions != null && instructions.size() > i ? instructions.get(i) : null));
        }
        try {
            Long doctorId = requireDoctorId(user);
            PrescriptionResponse p = emrService.createPrescription(new PrescriptionRequest(recordId, patientId, doctorId, items), user);
            redirectAttributes.addFlashAttribute("success", "Prescription issued.");
            return "redirect:/emr/prescriptions/" + p.id();
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/emr/prescriptions/new?patientId=" + patientId;
        }
    }

    @GetMapping("/prescriptions/{id}")
    public String prescriptionDetail(@PathVariable Long id, @CurrentUser SessionUser user, Model model) {
        model.addAttribute("prescription", emrService.getPrescription(id, user));
        return "emr/prescription-detail";
    }

    @GetMapping("/vitals/new")
    public String newVitalsForm(@RequestParam(required = false) Long patientId, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.DOCTOR, Role.NURSE);
        model.addAttribute("patientId", patientId);
        return "emr/vitals-form";
    }

    @PostMapping("/vitals/new")
    public String recordVitals(@CurrentUser SessionUser user,
                                @RequestParam Long patientId, @RequestParam(required = false) Long recordId,
                                @RequestParam(required = false) Integer bpSystolic, @RequestParam(required = false) Integer bpDiastolic,
                                @RequestParam(required = false) Integer pulse, @RequestParam(required = false) java.math.BigDecimal temperature,
                                @RequestParam(required = false) Integer spo2, @RequestParam(required = false) java.math.BigDecimal heightCm,
                                @RequestParam(required = false) java.math.BigDecimal weightKg,
                                RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.DOCTOR, Role.NURSE);
        try {
            emrService.recordVitals(new VitalsRequest(patientId, recordId, bpSystolic, bpDiastolic, pulse, temperature,
                    spo2, heightCm, weightKg), user);
            redirectAttributes.addFlashAttribute("success", "Vitals recorded.");
            return "redirect:/emr/patients/" + patientId + "/history";
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/emr/vitals/new?patientId=" + patientId;
        }
    }
}
