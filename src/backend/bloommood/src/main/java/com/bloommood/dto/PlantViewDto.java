package com.bloommood.dto;

import com.bloommood.entity.PlantStatus;
import com.bloommood.entity.PlantType;

import java.time.LocalDate;

public class PlantViewDto {

    private Long pid;
    private LocalDate plantDate;
    private PlantType type;
    private PlantStatus status;
    private Integer stage;

    public PlantViewDto() {
    }

    public PlantViewDto(Long pid, LocalDate plantDate, PlantType type, PlantStatus status, Integer stage) {
        this.pid = pid;
        this.plantDate = plantDate;
        this.type = type;
        this.status = status;
        this.stage = stage;
    }

    public Long getPid() { return pid; }
    public void setPid(Long pid) { this.pid = pid; }

    public LocalDate getPlantDate() { return plantDate; }
    public void setPlantDate(LocalDate plantDate) { this.plantDate = plantDate; }

    public PlantType getType() { return type; }
    public void setType(PlantType type) { this.type = type; }

    public PlantStatus getStatus() { return status; }
    public void setStatus(PlantStatus status) { this.status = status; }

    public Integer getStage() { return stage; }
    public void setStage(Integer stage) { this.stage = stage; }
}

