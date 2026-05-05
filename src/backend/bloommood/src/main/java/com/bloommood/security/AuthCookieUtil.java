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
//  設cookie，max-age是過期時間，secure是https才會傳送cookie，same-site是防止csrf攻擊
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
// Max-age設0來直接刪掉 cookie
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
// 讀cookie後面的jwt token
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
