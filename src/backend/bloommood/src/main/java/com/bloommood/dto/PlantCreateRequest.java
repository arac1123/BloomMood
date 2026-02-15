package com.bloommood.dto;

import com.bloommood.entity.PlantType;

/**
 * Optional request body for creating today's plant.
 * If type is null, service will pick a default.
 */
public class PlantCreateRequest {

    private PlantType type;

    public PlantCreateRequest() {
    }

    public PlantCreateRequest(PlantType type) {
        this.type = type;
    }

    public PlantType getType() { return type; }
    public void setType(PlantType type) { this.type = type; }
}

