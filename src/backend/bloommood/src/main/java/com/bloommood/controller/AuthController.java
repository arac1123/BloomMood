package com.bloommood.controller;

import com.bloommood.dto.GoogleAuthCodeRequest;
import com.bloommood.dto.LoginRequest;
import com.bloommood.dto.RegisterRequest;
import com.bloommood.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        System.out.println("Register request: " + req.getEmail() + ", " + req.getUname());
        if (req.getEmail() == null || req.getEmail().isBlank()
                || req.getUname() == null || req.getUname().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "email/uname/password required"));
        }

        try {
            authService.registerLocal(req.getEmail(), req.getUname(), req.getPassword());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        if (req.getEmail() == null || req.getEmail().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "email/password required"));
        }

        try {
            authService.loginLocal(req.getEmail(), req.getPassword(), response);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> google(@RequestBody GoogleAuthCodeRequest req, HttpServletResponse response) {
        if (req.getCode() == null || req.getCode().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "code required"));
        }

        try {
            authService.loginGoogleByCode(req.getCode(), response);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            // invalid_grant / id_token missing / invalid token / email exists ...
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "google code exchange failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}






