package com.bloommood.dto;

import com.bloommood.entity.PlantStatus;
import com.bloommood.entity.PlantType;

/**
 * Patch fields for updating a plant.
 * Any field that is null will be ignored.
 */
public class PlantUpdateRequest {

    private PlantType type;
    private PlantStatus status;
    private Integer stage;

    public PlantUpdateRequest() {
    }

    public PlantType getType() {
        return type;
    }

    public void setType(PlantType type) {
        this.type = type;
    }

    public PlantStatus getStatus() {
        return status;
    }

    public void setStatus(PlantStatus status) {
        this.status = status;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }
}

