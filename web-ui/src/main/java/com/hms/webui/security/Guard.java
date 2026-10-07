package com.hms.webui.security;

import com.hms.common.security.Role;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Simple in-controller role gate: throws a 403 (rendered via templates/error/403.html) when denied. */
public final class Guard {

    private Guard() {
    }

    public static void require(SessionUser user, Role... allowed) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in");
        }
        for (Role r : allowed) {
            if (r == user.role()) return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to do that");
    }
}
