package com.hms.webui.config;

import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUserArgumentResolver;
import com.hms.webui.security.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Injects the logged-in user into every view's model (so the nav fragment can read it) and
 * turns backend API failures into a flash message on a redirect back to where the user was,
 * rather than a hard error page — most ApiExceptions are recoverable (validation, conflicts).
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @ModelAttribute("currentUser")
    public SessionUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (SessionUser) session.getAttribute(CurrentUserArgumentResolver.SESSION_KEY);
    }

    @ExceptionHandler(ApiException.class)
    public String handleApiException(ApiException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("API call failed ({}): {}", ex.statusCode(), ex.getMessage());

        if (ex.statusCode() == 401) {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Your session has expired. Please log in again.");
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        if (!ex.errors().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorDetails", ex.errors());
        }

        String referer = request.getHeader("Referer");
        String safeReferer = (referer != null && referer.contains(request.getServerName())) ? referer : null;
        return "redirect:" + (safeReferer != null ? safeReferer : "/dashboard");
    }
}
