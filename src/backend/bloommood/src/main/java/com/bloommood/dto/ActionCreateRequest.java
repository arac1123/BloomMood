package com.bloommood.dto;

import com.bloommood.entity.ActionType;

public class ActionCreateRequest {

    private ActionType type;

    public ActionCreateRequest() {
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }
}

