package com.hms.webui.web;

import com.hms.common.security.Role;
import com.hms.webui.dto.AuthDtos.AuthResponse;
import com.hms.webui.dto.AuthDtos.LoginRequest;
import com.hms.webui.dto.AuthDtos.RegistrationRequest;
import com.hms.webui.dto.AuthDtos.UserResponse;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.CurrentUserArgumentResolver;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
public class AuthWebController {

    private final AuthService authService;

    public AuthWebController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String next,
                             @RequestParam(required = false) String expired,
                             Model model) {
        model.addAttribute("next", next);
        if (expired != null) {
            model.addAttribute("error", "Your session has expired. Please log in again.");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String usernameOrEmail,
                         @RequestParam String password,
                         @RequestParam(required = false) String next,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        try {
            AuthResponse auth = authService.login(new LoginRequest(usernameOrEmail, password));
            if (auth == null || auth.user() == null) {
                redirectAttributes.addFlashAttribute("error", "Login failed — please try again.");
                return "redirect:/login";
            }
            UserResponse u = auth.user();
            SessionUser sessionUser = new SessionUser(u.id(), u.username(), u.email(), u.firstName(), u.lastName(),
                    u.role(), auth.accessToken(), auth.refreshToken());
            HttpSession session = request.getSession(true);
            session.setAttribute(CurrentUserArgumentResolver.SESSION_KEY, sessionUser);
            redirectAttributes.addFlashAttribute("success", "Welcome back, " + u.firstName() + "!");
            if (next != null && !next.isBlank() && next.startsWith("/")) {
                return "redirect:" + next;
            }
            return "redirect:/dashboard";
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        // ADMIN is left out: the backend refuses self-registration as ADMIN.
        model.addAttribute("roles", Arrays.stream(Role.values()).filter(r -> r != Role.ADMIN).toList());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam String firstName,
                            @RequestParam String lastName,
                            @RequestParam Role role,
                            RedirectAttributes redirectAttributes) {
        try {
            authService.register(new RegistrationRequest(username, email, password, firstName, lastName, role));
            redirectAttributes.addFlashAttribute("success", "Account created. Please log in.");
            return "redirect:/login";
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(@CurrentUser SessionUser user, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (user != null) {
            try {
                authService.logout(user);
            } catch (ApiException ignored) {
                // best-effort server-side revoke; the session is cleared regardless
            }
        }
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You've been logged out.");
        return "redirect:/login";
    }
}
