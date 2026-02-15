package com.bloommood.controller;

import com.bloommood.dto.UpdateRequest;
import com.bloommood.dto.UserViewDto;
import com.bloommood.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }
    // 拿uid (如果没有认证信息或格式不对，返回null)，getName會對應到token第一個參數（目前是uid），所以這裡直接轉成Long
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

        try {
            UserViewDto dto = userService.getInfo(uid);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping
    public ResponseEntity<?> updateMe(@RequestBody UpdateRequest req, Authentication auth) {
        Long uid = currentUid(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("message", "unauthorized"));

        try {
            boolean changed = userService.updateMe(uid, req);
            return ResponseEntity.ok(Map.of("ok", true, "changed", changed));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
