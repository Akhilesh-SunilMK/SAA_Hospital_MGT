package com.hms.webui.web;

import com.hms.common.security.Role;
import com.hms.webui.dto.DoctorDtos.*;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.Guard;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.DoctorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/doctors")
public class DoctorWebController {

    private final DoctorService doctorService;

    public DoctorWebController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String specialisation,
                        @RequestParam(required = false) String department,
                        @CurrentUser SessionUser user, Model model) {
        model.addAttribute("results", doctorService.search(specialisation, department, user));
        model.addAttribute("specialisation", specialisation);
        model.addAttribute("department", department);
        return "doctors/list";
    }

    @GetMapping("/new")
    public String newForm(@CurrentUser SessionUser user) {
        Guard.require(user, Role.ADMIN);
        return "doctors/new";
    }

    @PostMapping("/new")
    public String create(@CurrentUser SessionUser user,
                          @RequestParam Long userId, @RequestParam String firstName, @RequestParam String lastName,
                          @RequestParam String registrationNo, @RequestParam String qualification,
                          @RequestParam String specialisation, @RequestParam String department,
                          @RequestParam BigDecimal consultationFee, RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.ADMIN);
        try {
            DoctorResponse doc = doctorService.create(new DoctorRequest(userId, firstName, lastName, registrationNo,
                    qualification, specialisation, department, consultationFee), user);
            redirectAttributes.addFlashAttribute("success", "Doctor profile created.");
            return "redirect:/doctors/" + doc.id();
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/doctors/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(required = false) LocalDate date,
                          @CurrentUser SessionUser user, Model model) {
        model.addAttribute("doctor", doctorService.get(id, user));
        model.addAttribute("date", date);
        if (date != null) {
            try {
                model.addAttribute("availability", doctorService.availability(id, date, user));
            } catch (ApiException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "doctors/detail";
    }

    @GetMapping("/{id}/schedule")
    public String scheduleForm(@PathVariable Long id, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.DOCTOR, Role.ADMIN);
        model.addAttribute("doctor", doctorService.get(id, user));
        model.addAttribute("dayOptions", List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"));
        return "doctors/schedule";
    }

    @PostMapping("/{id}/schedule")
    public String saveSchedule(@PathVariable Long id, @CurrentUser SessionUser user,
                                @RequestParam List<String> dayOfWeek,
                                @RequestParam List<LocalTime> startTime,
                                @RequestParam List<LocalTime> endTime,
                                @RequestParam List<Integer> slotDurationMin,
                                RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.DOCTOR, Role.ADMIN);
        List<SlotRequest> slots = new ArrayList<>();
        for (int i = 0; i < dayOfWeek.size(); i++) {
            slots.add(new SlotRequest(dayOfWeek.get(i), startTime.get(i), endTime.get(i), slotDurationMin.get(i)));
        }
        try {
            List<ScheduleResponse> saved = doctorService.setSchedule(id, new ScheduleRequest(slots), user);
            redirectAttributes.addFlashAttribute("success", "Weekly schedule saved.");
            redirectAttributes.addFlashAttribute("savedSchedule", saved);
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctors/" + id + "/schedule";
    }

    @PostMapping("/{id}/leave")
    public String addLeave(@PathVariable Long id, @CurrentUser SessionUser user,
                            @RequestParam LocalDate fromDate, @RequestParam LocalDate toDate,
                            @RequestParam(required = false) String reason, RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.DOCTOR, Role.ADMIN);
        try {
            doctorService.addLeave(id, new LeaveRequest(fromDate, toDate, reason), user);
            redirectAttributes.addFlashAttribute("success", "Leave recorded.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctors/" + id;
    }
}
