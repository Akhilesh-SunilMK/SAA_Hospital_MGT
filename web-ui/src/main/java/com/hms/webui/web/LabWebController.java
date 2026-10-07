package com.hms.webui.web;

import com.hms.common.security.Role;
import com.hms.webui.dto.LabDtos.*;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.Guard;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.DoctorService;
import com.hms.webui.service.LabService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/lab")
public class LabWebController {

    private final LabService labService;
    private final DoctorService doctorService;

    public LabWebController(LabService labService, DoctorService doctorService) {
        this.labService = labService;
        this.doctorService = doctorService;
    }

    @GetMapping("/catalogue")
    public String catalogue(@CurrentUser SessionUser user, Model model) {
        model.addAttribute("tests", labService.catalogue(user));
        return "lab/catalogue";
    }

    @GetMapping("/orders")
    public String lookup(@RequestParam(required = false) Long id, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.LAB_TECH, Role.DOCTOR, Role.ADMIN);
        if (id != null) {
            try {
                model.addAttribute("order", labService.getOrder(id, user));
            } catch (ApiException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "lab/order-lookup";
    }

    @GetMapping("/orders/new")
    public String newOrderForm(@RequestParam(required = false) Long patientId, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.DOCTOR);
        model.addAttribute("patientId", patientId);
        model.addAttribute("tests", labService.catalogue(user));
        return "lab/order-form";
    }

    @PostMapping("/orders/new")
    public String createOrder(@CurrentUser SessionUser user,
                               @RequestParam Long patientId, @RequestParam List<Long> testIds,
                               @RequestParam(defaultValue = "NORMAL") String priority,
                               @RequestParam(defaultValue = "false") boolean fastingRequired,
                               RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.DOCTOR);
        try {
            var doctor = doctorService.findMine(user);
            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Your login isn't linked to a doctor profile yet.");
                return "redirect:/lab/orders/new?patientId=" + patientId;
            }
            LabOrderResponse order = labService.createOrder(new LabOrderRequest(patientId, doctor.id(), testIds, priority, fastingRequired), user);
            redirectAttributes.addFlashAttribute("success", "Lab order #" + order.id() + " created.");
            return "redirect:/lab/orders?id=" + order.id();
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lab/orders/new?patientId=" + patientId;
        }
    }

    @PostMapping("/orders/{id}/status")
    public String updateStatus(@PathVariable Long id, @CurrentUser SessionUser user,
                                @RequestParam String status, RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.LAB_TECH);
        try {
            labService.updateStatus(id, new StatusUpdateRequest(status), user);
            redirectAttributes.addFlashAttribute("success", "Order status updated to " + status + ".");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lab/orders?id=" + id;
    }

    @PostMapping("/orders/{id}/results")
    public String uploadResult(@PathVariable Long id, @CurrentUser SessionUser user,
                                @RequestParam Long orderItemId, @RequestParam String value,
                                @RequestParam(required = false) String unit, @RequestParam(required = false) String referenceRange,
                                @RequestParam(required = false) Boolean abnormalFlag,
                                RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.LAB_TECH);
        try {
            labService.uploadResult(id, new ResultUploadRequest(orderItemId, value, unit, referenceRange, abnormalFlag), user);
            redirectAttributes.addFlashAttribute("success", "Result recorded.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lab/orders?id=" + id;
    }
}
