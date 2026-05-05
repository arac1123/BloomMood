package com.bloommood.dto;

import java.time.LocalDate;

public class MonthPlantDayDto {

    private LocalDate date;
    private boolean hasPlant;
    public PlantViewDto plant;

    public MonthPlantDayDto() {
    }

    public MonthPlantDayDto(LocalDate date, boolean hasPlant, PlantViewDto plant) {
        this.date = date;
        this.hasPlant = hasPlant;
        this.plant = plant;
    }

    public static MonthPlantDayDto empty(LocalDate date) {
        return new MonthPlantDayDto(date, false, null);
    }

    public static MonthPlantDayDto of(LocalDate date, PlantViewDto plant) {
        return new MonthPlantDayDto(date, true, plant);
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isHasPlant() { return hasPlant; }
    public void setHasPlant(boolean hasPlant) { this.hasPlant = hasPlant; }

    public PlantViewDto getPlant() { return plant; }
    public void setPlant(PlantViewDto plant) { this.plant = plant; }
}

