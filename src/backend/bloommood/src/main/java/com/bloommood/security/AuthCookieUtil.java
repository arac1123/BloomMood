package com.bloommood.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Small helper for setting/clearing the JWT HttpOnly cookie.
 *
 * Note: javax/jakarta Cookie API doesn't support SameSite directly, so we emit a Set-Cookie header.
 */
public final class AuthCookieUtil {

    private AuthCookieUtil() {}

    public static void setJwtCookie(HttpServletResponse response,
                                   String cookieName,
                                   String jwt,
                                   int maxAgeSeconds,
                                   boolean secure,
                                   String sameSite) {

        String ss = (sameSite == null || sameSite.isBlank()) ? "Lax" : sameSite;

        String header = cookieName + "=" + jwt
                + "; Path=/"
                + "; Max-Age=" + maxAgeSeconds
                + "; HttpOnly"
                + "; SameSite=" + ss;

        if (secure) {
            header += "; Secure";
        }

        response.addHeader("Set-Cookie", header);
    }

    public static void clearJwtCookie(HttpServletResponse response,
                                     String cookieName,
                                     boolean secure,
                                     String sameSite) {
        String ss = (sameSite == null || sameSite.isBlank()) ? "Lax" : sameSite;

        String header = cookieName + "="
                + "; Path=/"
                + "; Max-Age=0"
                + "; HttpOnly"
                + "; SameSite=" + ss;

        if (secure) {
            header += "; Secure";
        }

        response.addHeader("Set-Cookie", header);
    }

    public static String readJwtFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        for (var cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
