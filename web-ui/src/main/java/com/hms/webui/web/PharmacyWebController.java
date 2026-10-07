package com.hms.webui.web;

import com.hms.common.dto.PageResponse;
import com.hms.common.security.Role;
import com.hms.webui.dto.PharmacyDtos.*;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.Guard;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.PharmacyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/pharmacy")
public class PharmacyWebController {

    private final PharmacyService pharmacyService;

    public PharmacyWebController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @GetMapping("/drugs")
    public String drugs(@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page,
                         @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.DOCTOR, Role.NURSE, Role.RECEPTIONIST, Role.LAB_TECH, Role.PHARMACIST, Role.ACCOUNTANT, Role.ADMIN);
        PageResponse<DrugResponse> results = pharmacyService.drugs(search, page, 20, user);
        model.addAttribute("results", results);
        model.addAttribute("search", search);
        return "pharmacy/drugs";
    }

    @GetMapping("/drugs/new")
    public String newDrugForm(@CurrentUser SessionUser user) {
        Guard.require(user, Role.PHARMACIST, Role.ADMIN);
        return "pharmacy/drug-form";
    }

    @PostMapping("/drugs/new")
    public String createDrug(@CurrentUser SessionUser user,
                              @RequestParam String code, @RequestParam String genericName, @RequestParam(required = false) String brandName,
                              @RequestParam String form, @RequestParam String strength, @RequestParam(required = false) String manufacturer,
                              @RequestParam BigDecimal unitPrice, @RequestParam(required = false) Integer reorderLevel,
                              RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.PHARMACIST, Role.ADMIN);
        try {
            pharmacyService.createDrug(new DrugRequest(code, genericName, brandName, form, strength, manufacturer, unitPrice, reorderLevel), user);
            redirectAttributes.addFlashAttribute("success", "Drug added to catalogue.");
            return "redirect:/pharmacy/drugs";
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pharmacy/drugs/new";
        }
    }

    @PostMapping("/stock/{drugId}/adjust")
    public String adjustStock(@PathVariable Long drugId, @CurrentUser SessionUser user,
                               @RequestParam String batchNo, @RequestParam Integer quantity, @RequestParam LocalDate expiryDate,
                               RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.PHARMACIST);
        try {
            pharmacyService.adjustStock(drugId, new StockAdjustRequest(batchNo, quantity, expiryDate), user);
            redirectAttributes.addFlashAttribute("success", "Stock adjusted for drug #" + drugId + ".");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pharmacy/drugs";
    }

    @GetMapping("/dispense")
    public String dispenseForm(@RequestParam(required = false) Long patientId, @RequestParam(required = false) Long prescriptionId,
                                @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.PHARMACIST);
        model.addAttribute("patientId", patientId);
        model.addAttribute("prescriptionId", prescriptionId);
        return "pharmacy/dispense";
    }

    @PostMapping("/dispense")
    public String dispense(@CurrentUser SessionUser user,
                            @RequestParam Long patientId, @RequestParam(required = false) Long prescriptionId,
                            @RequestParam(defaultValue = "GENERAL") String patientCategory,
                            @RequestParam List<Long> drugId, @RequestParam List<Integer> quantity,
                            RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.PHARMACIST);
        List<DispenseItemRequest> items = new ArrayList<>();
        for (int i = 0; i < drugId.size(); i++) {
            items.add(new DispenseItemRequest(drugId.get(i), quantity.get(i)));
        }
        try {
            DispenseResponse d = pharmacyService.dispense(new DispenseRequest(patientId, prescriptionId, patientCategory, items), user);
            redirectAttributes.addFlashAttribute("success", "Dispensed " + d.itemCount() + " item(s), total " + d.totalAmount() + ".");
            return "redirect:/pharmacy/drugs";
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pharmacy/dispense?patientId=" + patientId;
        }
    }

    @GetMapping("/stock/low")
    public String lowStock(@CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.PHARMACIST, Role.ADMIN);
        model.addAttribute("items", pharmacyService.lowStock(user));
        return "pharmacy/low-stock";
    }
}
