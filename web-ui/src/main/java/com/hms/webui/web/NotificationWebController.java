package com.hms.webui.web;

import com.hms.common.security.Role;
import com.hms.webui.dto.NotificationDtos.*;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.Guard;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationWebController {

    private final NotificationService notificationService;

    public NotificationWebController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long userId, @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.ADMIN);
        model.addAttribute("results", notificationService.search(userId, status, page, 20, user));
        model.addAttribute("userId", userId);
        model.addAttribute("status", status);
        return "notifications/list";
    }

    @PostMapping("/send")
    public String send(@CurrentUser SessionUser user,
                        @RequestParam Long userId, @RequestParam String channel, @RequestParam String templateCode,
                        @RequestParam(required = false) List<String> varName, @RequestParam(required = false) List<String> varValue,
                        RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.ADMIN);
        Map<String, String> variables = new HashMap<>();
        if (varName != null) {
            for (int i = 0; i < varName.size(); i++) {
                if (varName.get(i) == null || varName.get(i).isBlank()) continue;
                variables.put(varName.get(i), varValue != null && varValue.size() > i ? varValue.get(i) : "");
            }
        }
        try {
            notificationService.send(new SendNotificationRequest(userId, channel, templateCode, variables), user);
            redirectAttributes.addFlashAttribute("success", "Notification queued.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/notifications";
    }

    @GetMapping("/preferences")
    public String preferencesForm(@CurrentUser SessionUser user) {
        return "notifications/preferences";
    }

    @PostMapping("/preferences")
    public String updatePreferences(@CurrentUser SessionUser user,
                                     @RequestParam String channel, @RequestParam(defaultValue = "false") boolean enabled,
                                     RedirectAttributes redirectAttributes) {
        try {
            notificationService.updatePreference(new UpdatePreferenceRequest(channel, enabled), user);
            redirectAttributes.addFlashAttribute("success", "Preference updated.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/notifications/preferences";
    }
}
