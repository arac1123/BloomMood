package com.bloommood.service;

import com.bloommood.entity.Action;
import com.bloommood.entity.ActionType;
import com.bloommood.entity.User;
import com.bloommood.repository.ActionRepository;
import com.bloommood.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ActionServiceTests {

    @Autowired
    ActionService actionService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ActionRepository actionRepository;

    @Test
    void createAction_persists() {
        User u = userRepository.save(new User("asvc@test.com", "A", "hash", "ROLE_USER"));
        Action a = actionService.createAction(u.getUid(), ActionType.WATER);
        assertNotNull(a.getId());
        assertEquals(ActionType.WATER, a.getType());
    }

    @Test
    void getActionsForMonth_returnsList() {
        User u = userRepository.save(new User("asvc2@test.com", "A2", "hash", "ROLE_USER"));
        actionService.createAction(u.getUid(), ActionType.SUN);

        var list = actionService.getActionsForMonth(u.getUid(), YearMonth.now());
        assertFalse(list.isEmpty());
    }
}
