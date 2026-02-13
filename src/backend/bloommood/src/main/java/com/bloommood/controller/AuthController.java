package com.bloommood.controller;

import com.bloommood.dto.LoginRequest;
import com.bloommood.dto.LoginResponse;
import com.bloommood.dto.RegisterRequest;
import com.bloommood.entity.User;
import com.bloommood.repository.UserRepository;
import com.bloommood.security.JwtUtil;

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

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()
                || req.getUname() == null || req.getUname().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "email/uname/password required"));
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "email already exists"));
        }

        String hash = passwordEncoder.encode(req.getPassword());

        User user = new User(
                req.getEmail(),
                req.getUname(),
                hash,
                "ROLE_USER"
        );
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "email/password required"));
        }

        User user = userRepository.findByEmail(req.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "invalid credentials"));
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "invalid credentials"));
        }

        // ✅ token subject 用 Uid
        String token = jwtUtil.generateToken(String.valueOf(user.getUid()), user.getRole());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
