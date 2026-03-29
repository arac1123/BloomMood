package com.bloommood.service;

import com.bloommood.dto.MonthPlantDayDto;
import com.bloommood.dto.PlantUpdateRequest;
import com.bloommood.dto.PlantViewDto;
import com.bloommood.entity.Plant;
import com.bloommood.entity.PlantStatus;
import com.bloommood.entity.PlantType;
import com.bloommood.entity.User;
import com.bloommood.repository.PlantRepository;
import com.bloommood.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlantService {

    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    public PlantService(PlantRepository plantRepository, UserRepository userRepository) {
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Plant getTodayPlant(Long uid) {
        LocalDate today = LocalDate.now();
        Plant plant = plantRepository.findByUser_UidAndPlantDate(uid, today).orElse(null);
        if (plant == null) {
            return null;
        }
        refreshWitherStatus(plant, today);
        return plant;
    }

    @Transactional
    public Plant CreateTodayPlant(Long uid, PlantType requestedType) {
        LocalDate today = LocalDate.now();

        Plant existing = plantRepository.findByUser_UidAndPlantDate(uid, today).orElse(null);
        if (existing != null) {
            throw new IllegalArgumentException("plant already exists for today");
        }
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }

        PlantType type = (requestedType == null) ? PlantType.FLOWER : requestedType;
        Plant plant = new Plant(user, today, type);

        try {
            return plantRepository.save(plant);
        } catch (DataIntegrityViolationException e) {
            // Concurrent request: (uid, plant_date) unique constraint hit.
            return plantRepository.findByUser_UidAndPlantDate(uid, today)
                    .orElseThrow(() -> e);
        }
    }

    @Transactional(readOnly = true)
    public List<Plant> getPlantsForMonth(Long uid, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return plantRepository.findAllByUser_UidAndPlantDateBetweenOrderByPlantDateAsc(uid, start, end);
    }

    @Transactional
    public List<MonthPlantDayDto> getMonthCalendar(Long uid, YearMonth month) {
        List<Plant> planted = getPlantsForMonth(uid, month);
        LocalDate today = LocalDate.now();

        planted.forEach(p -> refreshWitherStatus(p, today));

        Map<LocalDate, Plant> byDate = planted.stream()
                .collect(Collectors.toMap(Plant::getPlantDate, p -> p));

        int days = month.lengthOfMonth();
        java.util.ArrayList<MonthPlantDayDto> out = new java.util.ArrayList<>(days);

        for (int d = 1; d <= days; d++) {
            LocalDate date = month.atDay(d);
            Plant p = byDate.get(date);
            if (p == null) {
                out.add(MonthPlantDayDto.empty(date));
            } else {
                PlantViewDto dto = toViewDto(p);
                out.add(MonthPlantDayDto.of(date, dto));
            }
        }

        return out;
    }

    @Transactional
    public Plant updatePlant(Long uid, Long pid, PlantUpdateRequest req) {
        Plant plant = plantRepository.findByPidAndUser_Uid(pid, uid).orElse(null);
        if (plant == null) {
            throw new IllegalArgumentException("plant not found");
        }

        boolean changed = false;

        if (req.getType() != null) {
            plant.setType(req.getType());
            changed = true;
        }
        if (req.getStatus() != null) {
            plant.setStatus(req.getStatus());
            changed = true;
        }
        if (req.getStage() != null) {
            if (req.getStage() < 0) {
                throw new IllegalArgumentException("stage must be >= 0");
            }
            plant.setStage(req.getStage());
            changed = true;
        }

        if (!changed) {
            return plant;
        }

        return plantRepository.save(plant);
    }

    @Transactional
    public void deletePlant(Long uid, Long pid) {
        Plant plant = plantRepository.findByPidAndUser_Uid(pid, uid).orElse(null);
        if (plant == null) {
            throw new IllegalArgumentException("plant not found");
        }
        plantRepository.delete(plant);
    }

    public PlantViewDto toViewDto(Plant p) {
        return new PlantViewDto(
                p.getPid(),
                p.getPlantDate(),
                p.getType(),
                p.getStatus(),
                p.getStage(),
                p.getLastInteraction()
        );
    }

    private void refreshWitherStatus(Plant plant, LocalDate today) {
        if (plant.getStatus() == PlantStatus.DEAD) {
            return;
        }
        LocalDate lastInteraction = plant.getLastInteraction();
        if (lastInteraction == null) {
            lastInteraction = plant.getPlantDate();
            plant.setLastInteraction(lastInteraction);
        }

        long idleDays = ChronoUnit.DAYS.between(lastInteraction, today);
        if (plant.getStatus() == PlantStatus.NORMAL && idleDays >= 7) {
            plant.setStatus(PlantStatus.WITHERED);
        }
    }
}
