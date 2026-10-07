package com.hms.webui.service;

import com.hms.webui.client.ApiClient;
import com.hms.webui.dto.AuthDtos.AuthResponse;
import com.hms.webui.dto.AuthDtos.ForgotPasswordRequest;
import com.hms.webui.dto.AuthDtos.LoginRequest;
import com.hms.webui.dto.AuthDtos.RegistrationRequest;
import com.hms.webui.dto.AuthDtos.UserResponse;
import com.hms.webui.security.SessionUser;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final ApiClient api;

    public AuthService(ApiClient api) {
        this.api = api;
    }

    public UserResponse register(RegistrationRequest request) {
        return api.post("/api/v1/auth/register", request, UserResponse.class, null);
    }

    public AuthResponse login(LoginRequest request) {
        return api.post("/api/v1/auth/login", request, AuthResponse.class, null);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        api.post("/api/v1/auth/forgot-password", request, (Class<Void>) null, null);
    }

    public void logout(SessionUser user) {
        api.post("/api/v1/auth/logout", null, (Class<Void>) null, user);
    }

    public UserResponse me(SessionUser user) {
        return api.get("/api/v1/auth/me", UserResponse.class, user);
    }
}
