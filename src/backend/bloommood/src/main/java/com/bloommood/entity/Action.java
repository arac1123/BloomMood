package com.bloommood.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "action", indexes = {
        @Index(name = "idx_action_uid_time", columnList = "uid, action_time"),
        @Index(name = "idx_action_uid_type_time", columnList = "uid, type, action_time")
})
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionType type;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    public Action() {
    }

    public Action(User user, ActionType type, LocalDateTime actionTime) {
        this.user = user;
        this.type = type;
        this.actionTime = actionTime;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public LocalDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalDateTime actionTime) {
        this.actionTime = actionTime;
    }
}

