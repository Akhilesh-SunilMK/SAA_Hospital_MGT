package com.hms.webui.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

/** Redirects anonymous visitors to /login for every path except the small public whitelist. */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // getServletPath() (unlike getRequestURI()) never carries the context path or a
        // ";jsessionid=..." matrix parameter, which the container appends to the first
        // redirect after a session is created (e.g. to stash flash attributes) — comparing
        // against getRequestURI() would misclassify that redirect as a non-public path.
        String path = request.getServletPath();
        if (isPublic(path)) {
            return true;
        }
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(CurrentUserArgumentResolver.SESSION_KEY);
        if (user != null) {
            return true;
        }
        String query = request.getQueryString();
        String next = path + (query != null ? "?" + query : "");
        response.sendRedirect(request.getContextPath() + "/login?next=" + UriUtils.encode(next, StandardCharsets.UTF_8));
        return false;
    }

    private boolean isPublic(String path) {
        return path.equals("/login")
                || path.equals("/register")
                || path.equals("/logout")
                || path.equals("/")
                || path.equals("/error")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/webjars/")
                || path.startsWith("/actuator/");
    }
}
