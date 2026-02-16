package com.bloommood.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final String jwtCookieName;

    public JwtAuthFilter(JwtUtil jwtUtil, String jwtCookieName) {
        this.jwtUtil = jwtUtil;
        this.jwtCookieName = (jwtCookieName == null || jwtCookieName.isBlank()) ? "accessToken" : jwtCookieName;
    }

    // auth 跟 debug 不用看 token
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") || path.startsWith("/api/debug/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) Prefer cookie (HttpOnly)
        String token = AuthCookieUtil.readJwtFromCookie(request, jwtCookieName);

        // 2) Fallback to Authorization header for backwards compatibility / debugging
        if (token == null || token.isBlank()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }
        }

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.parseClaims(token);
            String uid = claims.getSubject();
            String role = claims.get("role", String.class);

            // jwt 裡面的 3 個 claim：uid / role
            var authToken = new UsernamePasswordAuthenticationToken(
                    uid,
                    null,
                    List.of(new SimpleGrantedAuthority(role == null ? "ROLE_USER" : role))
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
