package com.bloommood.dto;

import com.bloommood.entity.ActionType;

import java.time.LocalDateTime;

public class ActionViewDto {

    private Long id;
    private ActionType type;
    private LocalDateTime actionTime;

    public ActionViewDto() {
    }

    public ActionViewDto(Long id, ActionType type, LocalDateTime actionTime) {
        this.id = id;
        this.type = type;
        this.actionTime = actionTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ActionType getType() { return type; }
    public void setType(ActionType type) { this.type = type; }

    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
}

