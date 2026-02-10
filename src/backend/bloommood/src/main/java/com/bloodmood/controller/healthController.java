package com.bloodmood.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class healthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "ok", true,
                "message", "backend alive"
        );
    }
}