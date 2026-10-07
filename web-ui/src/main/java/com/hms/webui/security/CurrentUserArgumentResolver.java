package com.hms.webui.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.hms.webui.exception.ApiException;
import com.hms.webui.service.PatientService;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String SESSION_KEY = "currentUser";

    private final PatientService patientService;

    // @Lazy: PatientService -> ApiClient is wired after the MVC config that registers this resolver.
    public CurrentUserArgumentResolver(@Lazy PatientService patientService) {
        this.patientService = patientService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && SessionUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) return null;
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object user = session.getAttribute(SESSION_KEY);
        if (user instanceof SessionUser su && su.isPatient() && su.patientId() == null) {
            // Retried on each request until linked: the profile is often registered after the login exists.
            try {
                su.linkPatient(patientService.findMyPatientId(su));
            } catch (ApiException ignored) {
                // leave unlinked; pages already render a "no profile yet" state
            }
        }
        return user;
    }
}
