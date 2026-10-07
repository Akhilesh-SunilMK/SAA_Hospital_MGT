package com.hms.webui.web;

import com.hms.common.security.Role;
import com.hms.webui.dto.AppointmentDtos.*;
import com.hms.webui.dto.DoctorDtos.DoctorResponse;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.Guard;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.AppointmentService;
import com.hms.webui.service.DoctorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/appointments")
public class AppointmentWebController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    public AppointmentWebController(AppointmentService appointmentService, DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long patientId, @RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) LocalDate date, @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page,
                        @CurrentUser SessionUser user, Model model) {
        Long effectivePatientId = user.isPatient() ? user.patientId() : patientId;
        model.addAttribute("results", appointmentService.search(effectivePatientId, doctorId, date, status, page, 20, user));
        model.addAttribute("patientId", patientId);
        model.addAttribute("doctorId", doctorId);
        model.addAttribute("date", date);
        model.addAttribute("status", status);
        return "appointments/list";
    }

    @GetMapping("/book")
    public String bookForm(@RequestParam(required = false) Long patientId, @RequestParam(required = false) Long doctorId,
                            @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.PATIENT, Role.RECEPTIONIST, Role.ADMIN);
        model.addAttribute("doctors", doctorService.search(null, null, user));
        model.addAttribute("patientId", user.isPatient() ? user.patientId() : patientId);
        model.addAttribute("doctorId", doctorId);
        return "appointments/book";
    }

    @PostMapping("/book")
    public String book(@CurrentUser SessionUser user,
                        // The form omits patientId for PATIENT users, who always book for themselves.
                        @RequestParam(required = false) Long patientId, @RequestParam Long doctorId, @RequestParam LocalDateTime slot,
                        @RequestParam String type, @RequestParam(required = false) String reason,
                        @RequestParam(required = false) Integer durationMinutes,
                        RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.PATIENT, Role.RECEPTIONIST, Role.ADMIN);
        Long effectivePatientId = user.isPatient() ? user.patientId() : patientId;
        if (effectivePatientId == null) {
            redirectAttributes.addFlashAttribute("error", user.isPatient()
                    ? "Your login isn't linked to a patient profile yet. Ask reception to register you."
                    : "Patient ID is required.");
            return "redirect:/appointments/book?doctorId=" + doctorId;
        }
        try {
            AppointmentResponse a = appointmentService.book(new BookAppointmentRequest(effectivePatientId, doctorId, slot,
                    type, reason, durationMinutes, null, null), user);
            redirectAttributes.addFlashAttribute("success", "Appointment booked — token " + a.tokenNumber() + ".");
            return "redirect:/appointments/" + a.appointmentId();
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/appointments/book?doctorId=" + doctorId;
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @CurrentUser SessionUser user, Model model) {
        model.addAttribute("appointment", appointmentService.get(id, user));
        return "appointments/detail";
    }

    @PostMapping("/{id}/reschedule")
    public String reschedule(@PathVariable Long id, @CurrentUser SessionUser user,
                              @RequestParam LocalDateTime newSlot, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.reschedule(id, new RescheduleRequest(newSlot), user);
            redirectAttributes.addFlashAttribute("success", "Appointment rescheduled.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/appointments/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @CurrentUser SessionUser user, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancel(id, user);
            redirectAttributes.addFlashAttribute("success", "Appointment cancelled.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/appointments/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @CurrentUser SessionUser user,
                                @RequestParam String status, RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.DOCTOR, Role.RECEPTIONIST);
        try {
            appointmentService.updateStatus(id, new StatusUpdateRequest(status), user);
            redirectAttributes.addFlashAttribute("success", "Status updated to " + status + ".");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/appointments/" + id;
    }

    @GetMapping("/queue")
    public String queue(@RequestParam(required = false) Long doctorId, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.DOCTOR, Role.NURSE, Role.RECEPTIONIST, Role.ADMIN);
        Long effectiveDoctorId = doctorId;
        DoctorResponse mine = null;
        if (user.isDoctor()) {
            mine = doctorService.findMine(user);
            if (effectiveDoctorId == null && mine != null) effectiveDoctorId = mine.id();
        }
        model.addAttribute("doctors", doctorService.search(null, null, user));
        model.addAttribute("doctorId", effectiveDoctorId);
        if (effectiveDoctorId != null) {
            model.addAttribute("queue", appointmentService.queue(effectiveDoctorId, user));
        }
        return "appointments/queue";
    }
}
