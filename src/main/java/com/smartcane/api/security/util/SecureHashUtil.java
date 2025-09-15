package com.smartcane.api.security.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class SecureHashUtil {
    private SecureHashUtil() {}

    /** SHA-256 + hex (salt는 인프라/환경변수로 관리 권장) */
    public static String sha256Hex(String value) {
        if (value == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash value", e);
        }
    }
}
