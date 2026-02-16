package com.bloommood.repository;

import com.bloommood.entity.Action;
import com.bloommood.entity.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {

    List<Action> findAllByUser_UidAndActionTimeBetweenOrderByActionTimeAsc(Long uid, LocalDateTime startInclusive, LocalDateTime endExclusive);

    List<Action> findAllByUser_UidAndTypeAndActionTimeBetweenOrderByActionTimeAsc(Long uid, ActionType type, LocalDateTime startInclusive, LocalDateTime endExclusive);
}

