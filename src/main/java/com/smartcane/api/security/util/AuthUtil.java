package com.smartcane.api.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtil {
    private AuthUtil() {}
    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        Object p = auth.getPrincipal();
        return (p instanceof Long l) ? l : null;
    }
}
