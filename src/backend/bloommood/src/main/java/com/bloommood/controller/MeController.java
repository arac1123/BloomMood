package com.bloommood.controller;

import com.bloommood.dto.UpdateRequest;
import com.bloommood.dto.UserViewDto;
import com.bloommood.entity.User;
import com.bloommood.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MeController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Long currentUid(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/getInfo")
    public ResponseEntity<?> getInfo(Authentication auth) {
        Long uid = currentUid(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("message", "unauthorized"));

        User user = userRepository.findById(uid).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "user not found"));

        return ResponseEntity.ok(new UserViewDto(user.getUid(), user.getEmail(), user.getUname(), user.getRole()));
    }

    @PatchMapping
    public ResponseEntity<?> updateMe(@RequestBody UpdateRequest req, Authentication auth) {
        Long uid = currentUid(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("message", "unauthorized"));

        User user = userRepository.findById(uid).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "user not found"));

        boolean changed = false;

        // 1) uname（有傳才改）
        if (req.getUname() != null) {
            String newName = req.getUname().trim();
            if (newName.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "uname cannot be blank"));
            }
            user.setUname(newName);
            changed = true;
        }

        // 2) email（有傳才改）
        if (req.getEmail() != null) {
            String newEmail = req.getEmail().trim().toLowerCase();
            if (newEmail.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "email cannot be blank"));
            }

            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "email already exists"));
                }
                user.setEmail(newEmail);
                changed = true;
            }
        }

        // 3) password（只有 newPassword 有傳才處理）
        if (req.getNewPassword() != null) {
            String newPw = req.getNewPassword();
            if (newPw.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "newPassword cannot be blank"));
            }
            // 必須提供 oldPassword
            if (req.getOldPassword() == null || req.getOldPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "oldPassword required to change password"));
            }
            if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "invalid old password"));
            }

            user.setPassword(passwordEncoder.encode(newPw));
            changed = true;
        } else {
            // 你如果想防呆：有人只傳 oldPassword 沒傳 newPassword，就直接忽略 or 報錯
            // 我建議報錯比較清楚：
            if (req.getOldPassword() != null && !req.getOldPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "newPassword required when oldPassword is provided"));
            }
        }

        if (!changed) {
            return ResponseEntity.ok(Map.of("ok", true, "changed", false));
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("ok", true, "changed", true));
    }
}
