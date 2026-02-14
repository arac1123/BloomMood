package com.bloommood.controller;

import com.bloommood.dto.GoogleAuthCodeRequest;
import com.bloommood.dto.LoginRequest;
import com.bloommood.dto.RegisterRequest;
import com.bloommood.entity.AuthProvider;
import com.bloommood.entity.User;
import com.bloommood.repository.UserRepository;
import com.bloommood.security.AuthCookieUtil;
import com.bloommood.security.JwtUtil;
import com.bloommood.service.GoogleIdTokenVerifierService;
import com.bloommood.service.GoogleOAuthTokenService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GoogleIdTokenVerifierService googleVerifier;
    private final GoogleOAuthTokenService googleOAuthTokenService;

    @Value("${app.jwt.cookie.name:accessToken}")
    private String jwtCookieName;

    @Value("${app.jwt.cookie.max-age-seconds:3600}")
    private int jwtCookieMaxAgeSeconds;

    @Value("${app.jwt.cookie.secure:false}")
    private boolean jwtCookieSecure;

    @Value("${app.jwt.cookie.same-site:Lax}")
    private String jwtCookieSameSite;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          GoogleIdTokenVerifierService googleVerifier,
                          GoogleOAuthTokenService googleOAuthTokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.googleVerifier = googleVerifier;
        this.googleOAuthTokenService = googleOAuthTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()
                || req.getUname() == null || req.getUname().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "email/uname/password required"));
        }

        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "email already exists"));
        }

        String hash = passwordEncoder.encode(req.getPassword());

        User user = new User(email, req.getUname().trim(), hash, "ROLE_USER");
        user.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        if (req.getEmail() == null || req.getEmail().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "email/password required"));
        }

        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "invalid credentials"));
        }

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            return ResponseEntity.status(400).body(Map.of("message", "use google oauth"));
        }

        if (user.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "invalid credentials"));
        }

        String token = jwtUtil.generateToken(String.valueOf(user.getUid()), user.getRole());
        AuthCookieUtil.setJwtCookie(response, jwtCookieName, token, jwtCookieMaxAgeSeconds, jwtCookieSecure, jwtCookieSameSite);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * Authorization Code Flow (teaching version):
     * Frontend sends Google OAuth authorization code, backend exchanges it for tokens,
     * then verifies the returned id_token and issues our own JWT.
     */
    @PostMapping("/google")
    public ResponseEntity<?> google(@RequestBody GoogleAuthCodeRequest req, HttpServletResponse response) {
        if (req.getCode() == null || req.getCode().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "code required"));
        }
        final Map<String, Object> tokenResp;
        try {
            tokenResp = googleOAuthTokenService.exchangeCodeForTokens(req.getCode());
        } catch (Exception e) {
            // common errors: invalid_grant (code reused / redirect_uri mismatch)
            e.printStackTrace(); // 先看 console 真實原因
            return ResponseEntity.status(401).body(Map.of("message", "google code exchange failed"));
        }

        String idToken = tokenResp.get("id_token") == null ? null : tokenResp.get("id_token").toString();
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("message", "missing id_token from google"));
        }

        final Map<String, Object> claims;
        try {
            claims = googleVerifier.verify(idToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "invalid google token"));
        }

        String sub = claims.get("sub") == null ? null : claims.get("sub").toString();
        String email = claims.get("email") == null ? null : claims.get("email").toString();
        String name = claims.get("name") == null ? null : claims.get("name").toString();

        if (sub == null || sub.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("message", "invalid google token"));
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "google email missing"));
        }

        email = email.trim().toLowerCase();

        User user = userRepository.findByOauthSub(sub).orElse(null);
        if (user == null) {
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("message", "email already exists"));
            }
            String uname = (name == null || name.isBlank()) ? "GoogleUser" : name.trim();
            user = new User(email, uname, null, "ROLE_USER", AuthProvider.GOOGLE, sub);
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(String.valueOf(user.getUid()), user.getRole());
        AuthCookieUtil.setJwtCookie(response, jwtCookieName, token, jwtCookieMaxAgeSeconds, jwtCookieSecure, jwtCookieSameSite);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        AuthCookieUtil.clearJwtCookie(response, jwtCookieName, jwtCookieSecure, jwtCookieSameSite);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
