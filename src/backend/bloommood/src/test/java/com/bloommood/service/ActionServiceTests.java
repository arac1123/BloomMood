package com.bloommood.service;

import com.bloommood.entity.Action;
import com.bloommood.entity.ActionType;
import com.bloommood.entity.Plant;
import com.bloommood.entity.PlantStatus;
import com.bloommood.entity.PlantType;
import com.bloommood.entity.User;
import com.bloommood.repository.ActionRepository;
import com.bloommood.repository.PlantRepository;
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

    @Autowired
    PlantRepository plantRepository;

    @Test
    void createAction_persists() {
        User u = userRepository.save(new User("asvc@test.com", "A", "hash", "ROLE_USER"));
        plantRepository.save(new Plant(u, java.time.LocalDate.now(), PlantType.FLOWER));
        Action a = actionService.createAction(u.getUid(), ActionType.WATER);
        assertNotNull(a.getId());
        assertEquals(ActionType.WATER, a.getType());
    }

    @Test
    void getActionsForMonth_returnsList() {
        User u = userRepository.save(new User("asvc2@test.com", "A2", "hash", "ROLE_USER"));
        plantRepository.save(new Plant(u, java.time.LocalDate.now(), PlantType.TREE));
        actionService.createAction(u.getUid(), ActionType.SUN);

        var list = actionService.getActionsForMonth(u.getUid(), YearMonth.now());
        assertFalse(list.isEmpty());
    }

    @Test
    void createAction_sameDayOnlyGrowsOnceWithoutError() {
        User u = userRepository.save(new User("asvc3@test.com", "A3", "hash", "ROLE_USER"));
        Plant p = plantRepository.save(new Plant(u, java.time.LocalDate.now(), PlantType.CACTUS));

        actionService.createAction(u.getUid(), ActionType.SUN);
        actionService.createAction(u.getUid(), ActionType.FERTILIZE);
        actionService.createAction(u.getUid(), ActionType.WATER);
        actionService.createAction(u.getUid(), ActionType.SUN);

        Plant updated = plantRepository.findById(p.getPid()).orElseThrow();
        assertEquals(2, updated.getStage());
    }

    @Test
    void createAction_waterRecoversWitheredWithoutStageDrop() {
        User u = userRepository.save(new User("asvc4@test.com", "A4", "hash", "ROLE_USER"));
        Plant p = new Plant(u, java.time.LocalDate.now().minusDays(10), PlantType.FLOWER);
        p.setStage(4);
        p.setStatus(PlantStatus.WITHERED);
        p.setLastInteraction(java.time.LocalDate.now().minusDays(10));
        p = plantRepository.save(p);

        actionService.createAction(u.getUid(), ActionType.WATER);

        Plant updated = plantRepository.findById(p.getPid()).orElseThrow();
        assertEquals(PlantStatus.NORMAL, updated.getStatus());
        assertEquals(4, updated.getStage());
    }
}
