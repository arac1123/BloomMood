package com.bloommood.controller;

import com.bloommood.dto.MonthPlantDayDto;
import com.bloommood.dto.PlantCreateRequest;
import com.bloommood.dto.PlantUpdateRequest;
import com.bloommood.dto.PlantViewDto;
import com.bloommood.entity.Plant;
import com.bloommood.service.PlantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plant")
public class PlantController {

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    private Long currentUid(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private PlantViewDto toDto(Plant p) {
        return plantService.toViewDto(p);
    }

    /**
     * Get today's plant. If user hasn't planted today, returns: {"hasPlant": false}
     */
    @GetMapping("/today")
    public ResponseEntity<?> getToday(Authentication auth) {
        Long uid = currentUid(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("message", "unauthorized"));

        Plant plant = plantService.getTodayPlant(uid);
        if (plant == null) {
            return ResponseEntity.ok(Map.of("hasPlant", false));
        }
        return ResponseEntity.ok(Map.of("hasPlant", true, "plant", toDto(plant)));
    }

    /**
     * Get or create today's plant (1 plant per user per day).
     */
    @PostMapping("/today")
    public ResponseEntity<?> createOrGetToday(@RequestBody(required = false) PlantCreateRequest req, Authentication auth) {
        Long uid = currentUid(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("message", "unauthorized"));
        try{
            Plant plant = plantService.CreateTodayPlant(uid, req == null ? null : req.getType());
            return ResponseEntity.ok(toDto(plant));
        } catch ( IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

    }

    /**
     * Month view. Query param: ym=YYYY-MM
     * Example: /api/plant/month?ym=2026-02
     *
     * Returns a list with one entry per day; days with no plant will be hasPlant=false.
     */
    @GetMapping("/month")
    public ResponseEntity<?> getMonth(@RequestParam("ym") String ym, Authentication auth) {
        Long uid = currentUid(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("message", "unauthorized"));

        YearMonth month;
        try {
            month = YearMonth.parse(ym);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid ym, expected YYYY-MM"));
        }

        List<MonthPlantDayDto> out = plantService.getMonthCalendar(uid, month);
        return ResponseEntity.ok(out);
    }

    @PatchMapping("/{pid}")
    public ResponseEntity<?> updatePlant(@PathVariable Long pid, @RequestBody PlantUpdateRequest req, Authentication auth) {
        Long uid = currentUid(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("message", "unauthorized"));

        try {
            Plant updated = plantService.updatePlant(uid, pid, req);
            return ResponseEntity.ok(toDto(updated));
        } catch (IllegalArgumentException e) {
            // not found / bad stage
            String msg = e.getMessage();
            if ("plant not found".equalsIgnoreCase(msg)) {
                return ResponseEntity.status(404).body(Map.of("message", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    @DeleteMapping("/{pid}")
    public ResponseEntity<?> deletePlant(@PathVariable Long pid, Authentication auth) {
        Long uid = currentUid(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("message", "unauthorized"));

        try {
            plantService.deletePlant(uid, pid);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }
}
