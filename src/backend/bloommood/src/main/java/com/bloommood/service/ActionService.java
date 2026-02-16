package com.bloommood.service;

import com.bloommood.entity.Action;
import com.bloommood.entity.ActionType;
import com.bloommood.entity.User;
import com.bloommood.repository.ActionRepository;
import com.bloommood.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
public class ActionService {

    private final ActionRepository actionRepository;
    private final UserRepository userRepository;

    public ActionService(ActionRepository actionRepository, UserRepository userRepository) {
        this.actionRepository = actionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Action createAction(Long uid, ActionType type) {
        if (type == null) {
            throw new IllegalArgumentException("type required");
        }

        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }

        Action a = new Action(user, type, LocalDateTime.now());
        return actionRepository.save(a);
    }

    @Transactional(readOnly = true)
    public List<Action> getActionsForMonth(Long uid, YearMonth ym) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        // endExclusive: first moment of next month
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
        return actionRepository.findAllByUser_UidAndActionTimeBetweenOrderByActionTimeAsc(uid, start, end);
    }


}

