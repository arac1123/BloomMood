package com.bloommood.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "plant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "plant_date"})
)
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Column(name = "plant_date", nullable = false)
    private LocalDate plantDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlantStatus status = PlantStatus.NORMAL;

    @Column(nullable = false)
    private Integer stage = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlantType type = PlantType.FLOWER;

    public Plant() {
    }
    // Constructor for creating a new plant with default status and stage.
    public Plant(User user, LocalDate plantDate, PlantType type) {
        this.user = user;
        this.plantDate = plantDate;
        this.type = type;
        this.status = PlantStatus.NORMAL;
        this.stage = 0;
    }

    public Long getPid() {
        return pid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getPlantDate() {
        return plantDate;
    }

    public void setPlantDate(LocalDate plantDate) {
        this.plantDate = plantDate;
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

    public PlantType getType() {
        return type;
    }

    public void setType(PlantType type) {
        this.type = type;
    }
}
