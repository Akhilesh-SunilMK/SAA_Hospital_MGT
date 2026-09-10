package com.hms.appointment.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Forwards the original caller's bearer token to downstream validation calls (Doctor/Patient). */
@Component
public class RequestAuthHeaderProvider {

    public String currentAuthorizationHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "";
        }
        HttpServletRequest request = attrs.getRequest();
        String header = request.getHeader("Authorization");
        return header != null ? header : "";
    }
}
