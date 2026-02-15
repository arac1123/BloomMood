package com.bloommood.service;

import com.bloommood.entity.AuthProvider;
import com.bloommood.entity.User;
import com.bloommood.repository.UserRepository;
import com.bloommood.security.AuthCookieUtil;
import com.bloommood.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GoogleIdTokenVerifierService googleVerifier;
    private final GoogleOAuthTokenService googleOAuthTokenService;

    private final String jwtCookieName;
    private final int jwtCookieMaxAgeSeconds;
    private final boolean jwtCookieSecure;
    private final String jwtCookieSameSite;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       GoogleIdTokenVerifierService googleVerifier,
                       GoogleOAuthTokenService googleOAuthTokenService,
                       @Value("${app.jwt.cookie.name:accessToken}") String jwtCookieName,
                       @Value("${app.jwt.cookie.max-age-seconds:3600}") int jwtCookieMaxAgeSeconds,
                       @Value("${app.jwt.cookie.secure:false}") boolean jwtCookieSecure,
                       @Value("${app.jwt.cookie.same-site:Lax}") String jwtCookieSameSite) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.googleVerifier = googleVerifier;
        this.googleOAuthTokenService = googleOAuthTokenService;
        this.jwtCookieName = jwtCookieName;
        this.jwtCookieMaxAgeSeconds = jwtCookieMaxAgeSeconds;
        this.jwtCookieSecure = jwtCookieSecure;
        this.jwtCookieSameSite = jwtCookieSameSite;
    }
    // 1) Local註冊（email, uname, password）
    public void registerLocal(String emailRaw, String unameRaw, String passwordRaw) {
        String email = emailRaw.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("email already exists");
        }

        String hash = passwordEncoder.encode(passwordRaw);

        User user = new User(email, unameRaw.trim(), hash, "ROLE_USER");
        user.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(user);
    }
    // 2) Local登入（email, password）
    public void loginLocal(String emailRaw, String passwordRaw, HttpServletResponse response) {
        User user = userRepository.findByEmail(emailRaw.trim().toLowerCase()).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("invalid credentials");
        }

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new IllegalStateException("use google oauth");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(passwordRaw, user.getPassword())) {
            throw new IllegalArgumentException("invalid credentials");
        }

        issueJwtCookie(response, user);
    }
    // 3) Google登入（Google code exchange -> get id_token -> verify -> find or create user -> issue JWT cookie）
    public void loginGoogleByCode(String code, HttpServletResponse response) {
        final Map<String, Object> tokenResp = googleOAuthTokenService.exchangeCodeForTokens(code);

        String idToken = tokenResp.get("id_token") == null ? null : tokenResp.get("id_token").toString();
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("missing id_token from google");
        }

        final Map<String, Object> claims = googleVerifier.verify(idToken);

        String sub = claims.get("sub") == null ? null : claims.get("sub").toString();
        String email = claims.get("email") == null ? null : claims.get("email").toString();
        String name = claims.get("name") == null ? null : claims.get("name").toString();

        if (sub == null || sub.isBlank()) {
            throw new IllegalArgumentException("invalid google token");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("google email missing");
        }

        email = email.trim().toLowerCase();

        User user = userRepository.findByOauthSub(sub).orElse(null);
        if (user == null) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("email already exists");
            }
            String uname = (name == null || name.isBlank()) ? "GoogleUser" : name.trim();
            user = new User(email, uname, null, "ROLE_USER", AuthProvider.GOOGLE, sub);
            userRepository.save(user);
        }

        issueJwtCookie(response, user);
    }
    // 4) 登出（清除 JWT cookie）
    public void logout(HttpServletResponse response) {
        AuthCookieUtil.clearJwtCookie(response, jwtCookieName, jwtCookieSecure, jwtCookieSameSite);
    }
    // JWT cookie 相關設定（名稱、有效期、安全、SameSite）從 application.properties 注入
    private void issueJwtCookie(HttpServletResponse response, User user) {
        String token = jwtUtil.generateToken(String.valueOf(user.getUid()), user.getRole());
        AuthCookieUtil.setJwtCookie(response, jwtCookieName, token, jwtCookieMaxAgeSeconds, jwtCookieSecure, jwtCookieSameSite);
    }
}

