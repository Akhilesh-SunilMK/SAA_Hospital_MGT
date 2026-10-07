package com.hms.webui.web;

import com.hms.common.dto.PageResponse;
import com.hms.common.security.Role;
import com.hms.webui.dto.AuthDtos.RegistrationRequest;
import com.hms.webui.dto.AuthDtos.UserResponse;
import com.hms.webui.dto.PatientDtos.*;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.Guard;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.AuthService;
import com.hms.webui.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/patients")
public class PatientWebController {

    private final PatientService patientService;
    private final AuthService authService;

    public PatientWebController(PatientService patientService, AuthService authService) {
        this.patientService = patientService;
        this.authService = authService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page,
                        @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.DOCTOR, Role.NURSE, Role.RECEPTIONIST, Role.LAB_TECH, Role.PHARMACIST, Role.ACCOUNTANT, Role.ADMIN);
        PageResponse<PatientResponse> results = patientService.search(search, page, 20, user);
        model.addAttribute("results", results);
        model.addAttribute("search", search);
        return "patients/list";
    }

    @GetMapping("/new")
    public String newForm(@CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.RECEPTIONIST, Role.ADMIN);
        return "patients/new";
    }

    @PostMapping("/new")
    public String create(@CurrentUser SessionUser user,
                          @RequestParam String username, @RequestParam String email, @RequestParam String password,
                          @RequestParam String firstName, @RequestParam String lastName,
                          @RequestParam LocalDate dob, @RequestParam String gender, @RequestParam String phone,
                          @RequestParam(required = false) String address, @RequestParam(required = false) String bloodGroup,
                          @RequestParam(required = false) String allergiesSummary, @RequestParam(required = false) String chronicConditions,
                          RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.RECEPTIONIST, Role.ADMIN);
        try {
            UserResponse newUser = authService.register(new RegistrationRequest(username, email, password, firstName, lastName, Role.PATIENT));
            PatientResponse patient = patientService.create(new PatientRequest(firstName, lastName, dob, gender, phone,
                    email, address, bloodGroup, allergiesSummary, chronicConditions, newUser.id()), user);
            redirectAttributes.addFlashAttribute("success", "Patient " + patient.mrn() + " registered.");
            return "redirect:/patients/" + patient.id();
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/patients/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @CurrentUser SessionUser user, Model model) {
        PatientResponse patient = patientService.get(id, user);
        List<AllergyResponse> allergies = patientService.allergies(id, user);
        List<EmergencyContactResponse> contacts = patientService.emergencyContacts(id, user);
        model.addAttribute("patient", patient);
        model.addAttribute("allergies", allergies);
        model.addAttribute("contacts", contacts);
        return "patients/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.RECEPTIONIST, Role.ADMIN);
        model.addAttribute("patient", patientService.get(id, user));
        return "patients/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @CurrentUser SessionUser user,
                        @RequestParam String phone, @RequestParam(required = false) String email,
                        @RequestParam(required = false) String address, @RequestParam(required = false) String bloodGroup,
                        @RequestParam(required = false) String allergiesSummary, @RequestParam(required = false) String chronicConditions,
                        RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.RECEPTIONIST, Role.ADMIN);
        try {
            patientService.update(id, new PatientUpdateRequest(phone, email, address, bloodGroup, allergiesSummary, chronicConditions), user);
            redirectAttributes.addFlashAttribute("success", "Patient updated.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @CurrentUser SessionUser user, RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.ADMIN);
        try {
            patientService.delete(id, user);
            redirectAttributes.addFlashAttribute("success", "Patient record deactivated.");
            return "redirect:/patients";
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/patients/" + id;
        }
    }

    @PostMapping("/{id}/admit")
    public String admit(@PathVariable Long id, @CurrentUser SessionUser user,
                         @RequestParam Long wardId, @RequestParam String bedNo,
                         RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.RECEPTIONIST, Role.DOCTOR);
        try {
            patientService.admit(id, new AdmitRequest(wardId, bedNo), user);
            redirectAttributes.addFlashAttribute("success", "Patient admitted.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/discharge")
    public String discharge(@PathVariable Long id, @CurrentUser SessionUser user, RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.DOCTOR, Role.ADMIN);
        try {
            patientService.discharge(id, user);
            redirectAttributes.addFlashAttribute("success", "Patient discharged.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/allergies")
    public String addAllergy(@PathVariable Long id, @CurrentUser SessionUser user,
                              @RequestParam String allergen, @RequestParam String severity,
                              @RequestParam LocalDate notedOn, RedirectAttributes redirectAttributes) {
        try {
            patientService.addAllergy(id, new AllergyRequest(allergen, severity, notedOn), user);
            redirectAttributes.addFlashAttribute("success", "Allergy recorded.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/emergency-contacts")
    public String addContact(@PathVariable Long id, @CurrentUser SessionUser user,
                              @RequestParam String name, @RequestParam String relation, @RequestParam String phone,
                              RedirectAttributes redirectAttributes) {
        try {
            patientService.addEmergencyContact(id, new EmergencyContactRequest(name, relation, phone), user);
            redirectAttributes.addFlashAttribute("success", "Emergency contact added.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patients/" + id;
    }
}
