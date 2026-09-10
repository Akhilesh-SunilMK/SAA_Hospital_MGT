package com.hms.auth.dto;

import com.hms.auth.entity.User;
import com.hms.common.security.Role;

import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        Role role,
        Set<String> permissions
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole(), user.getPermissions());
    }
}
