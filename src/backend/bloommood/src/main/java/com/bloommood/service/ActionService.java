package com.bloommood.service;

import com.bloommood.entity.Action;
import com.bloommood.entity.ActionType;
import com.bloommood.entity.Plant;
import com.bloommood.entity.PlantStatus;
import com.bloommood.entity.User;
import com.bloommood.repository.ActionRepository;
import com.bloommood.repository.PlantRepository;
import com.bloommood.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ActionService {

    private final ActionRepository actionRepository;
    private final UserRepository userRepository;
    private final PlantRepository plantRepository;

    public ActionService(ActionRepository actionRepository, UserRepository userRepository, PlantRepository plantRepository) {
        this.actionRepository = actionRepository;
        this.userRepository = userRepository;
        this.plantRepository = plantRepository;
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

        Plant plant = plantRepository.findTopByUser_UidOrderByPlantDateDescPidDesc(uid).orElse(null);
        if (plant == null) {
            throw new IllegalArgumentException("no plant to interact with");
        }

        LocalDate today = LocalDate.now();
        applyWitherIfIdle(plant, today);

        int stage = plant.getStage() == null ? 1 : plant.getStage();
        if (stage < 4 && canGrowOnDate(plant.getLastInteraction(), today)) {
            plant.setStage(stage + 1);
        }

        plant.setLastInteraction(today);
        if (type == ActionType.WATER && plant.getStatus() != PlantStatus.DEAD) {
            plant.setStatus(PlantStatus.NORMAL);
        }
        plantRepository.save(plant);

        Action a = new Action(user, type, LocalDateTime.now());
        return actionRepository.save(a);
    }

    @Transactional(readOnly = true)
    public List<Action> getActionsForMonth(Long uid, YearMonth ym) {
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        // endExclusive: first moment of next month
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
        return actionRepository.findAllByUser_UidAndActionTimeBetweenOrderByActionTimeAsc(uid, start, end);
    }

    private void applyWitherIfIdle(Plant plant, LocalDate today) {
        if (plant.getStatus() == PlantStatus.DEAD) {
            return;
        }
        LocalDate lastInteraction = plant.getLastInteraction() == null ? plant.getPlantDate() : plant.getLastInteraction();
        long idleDays = ChronoUnit.DAYS.between(lastInteraction, today);
        if (plant.getStatus() == PlantStatus.NORMAL && idleDays >= 7) {
            plant.setStatus(PlantStatus.WITHERED);
        }
    }

    private boolean canGrowOnDate(LocalDate lastInteraction, LocalDate today) {
        // One stage growth per calendar day.
        return lastInteraction == null || lastInteraction.isBefore(today);
    }

}

