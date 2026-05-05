package com.bloommood.controller;

import com.bloommood.dto.ActionCreateRequest;
import com.bloommood.dto.ActionViewDto;
import com.bloommood.entity.Action;
import com.bloommood.service.ActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/action")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    private Long currentUid(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ActionViewDto toDto(Action a) {
        return new ActionViewDto(a.getId(), a.getType(), a.getActionTime());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ActionCreateRequest req, Authentication auth) {
        Long uid = currentUid(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("message", "unauthorized"));

        try {
            Action a = actionService.createAction(uid, req == null ? null : req.getType());
            return ResponseEntity.ok(toDto(a));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

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

        List<ActionViewDto> out = actionService.getActionsForMonth(uid, month).stream().map(this::toDto).toList();
        return ResponseEntity.ok(out);
    }
}

