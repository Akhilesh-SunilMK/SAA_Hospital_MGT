package com.hms.webui.web;

import com.hms.common.dto.PageResponse;
import com.hms.webui.dto.AppointmentDtos.AppointmentResponse;
import com.hms.webui.dto.DoctorDtos.DoctorResponse;
import com.hms.webui.dto.PatientDtos.PatientResponse;
import com.hms.webui.dto.PharmacyDtos.LowStockResponse;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.AppointmentService;
import com.hms.webui.service.DoctorService;
import com.hms.webui.service.PatientService;
import com.hms.webui.service.PharmacyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final PharmacyService pharmacyService;

    public HomeController(PatientService patientService, DoctorService doctorService,
                           AppointmentService appointmentService, PharmacyService pharmacyService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.pharmacyService = pharmacyService;
    }

    @GetMapping("/")
    public String root(@CurrentUser SessionUser user) {
        return user == null ? "redirect:/login" : "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@CurrentUser SessionUser user, Model model) {
        if (user.isPatient()) {
            PatientResponse profile = null;
            List<AppointmentResponse> upcoming = List.of();
            try {
                profile = patientService.get(user.patientId(), user);
                PageResponse<AppointmentResponse> page = appointmentService.search(user.patientId(), null, null, null, 0, 5, user);
                upcoming = page.content();
            } catch (ApiException ignored) {
                // no linked patient profile yet - the view explains how to get one
            }
            model.addAttribute("profile", profile);
            model.addAttribute("upcoming", upcoming);
        } else if (user.isDoctor()) {
            DoctorResponse mine = null;
            List<AppointmentResponse> queue = List.of();
            try {
                mine = doctorService.findMine(user);
                if (mine != null) {
                    queue = appointmentService.queue(mine.id(), user);
                }
            } catch (ApiException ignored) {
            }
            model.addAttribute("myDoctorProfile", mine);
            model.addAttribute("queue", queue);
        } else if (user.isPharmacist()) {
            List<LowStockResponse> lowStock = List.of();
            try {
                lowStock = pharmacyService.lowStock(user);
            } catch (ApiException ignored) {
            }
            model.addAttribute("lowStock", lowStock);
        }
        return "dashboard";
    }
}
