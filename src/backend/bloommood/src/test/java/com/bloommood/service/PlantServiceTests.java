package com.bloommood.service;

import com.bloommood.dto.MonthPlantDayDto;
import com.bloommood.dto.PlantUpdateRequest;
import com.bloommood.entity.Plant;
import com.bloommood.entity.PlantStatus;
import com.bloommood.entity.PlantType;
import com.bloommood.entity.User;
import com.bloommood.repository.PlantRepository;
import com.bloommood.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantServiceTests {

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlantService plantService;

    @Test
    void getTodayPlant_returnsNullWhenNoPlant() {
        when(plantRepository.findByUser_UidAndPlantDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.empty());

        Plant result = plantService.getTodayPlant(1L);

        assertNull(result);
    }

    @Test
    void getTodayPlant_setsWitheredWhenIdleSevenDays() {
        LocalDate today = LocalDate.now();
        Plant plant = new Plant();
        plant.setStatus(PlantStatus.NORMAL);
        plant.setPlantDate(today.minusDays(10));
        plant.setLastInteraction(today.minusDays(7));

        when(plantRepository.findByUser_UidAndPlantDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.of(plant));

        Plant result = plantService.getTodayPlant(1L);

        assertEquals(PlantStatus.WITHERED, result.getStatus());
    }

    @Test
    void createTodayPlant_throwsWhenAlreadyExists() {
        when(plantRepository.findByUser_UidAndPlantDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.of(new Plant()));

        assertThrows(IllegalArgumentException.class, () -> plantService.CreateTodayPlant(1L, PlantType.FLOWER));
    }

    @Test
    void createTodayPlant_throwsWhenUserMissing() {
        when(plantRepository.findByUser_UidAndPlantDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> plantService.CreateTodayPlant(1L, PlantType.FLOWER));
    }

    @Test
    void createTodayPlant_defaultsTypeToFlower() {
        User user = new User();

        when(plantRepository.findByUser_UidAndPlantDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(plantRepository.save(any(Plant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plant result = plantService.CreateTodayPlant(1L, null);

        assertEquals(PlantType.FLOWER, result.getType());
        assertEquals(LocalDate.now(), result.getPlantDate());
    }

    @Test
    void getPlantsForMonth_returnsRepositoryResult() {
        YearMonth month = YearMonth.of(2026, 4);
        Plant plant = new Plant();
        when(plantRepository.findAllByUser_UidAndPlantDateBetweenOrderByPlantDateAsc(eq(1L), eq(month.atDay(1)), eq(month.atEndOfMonth())))
                .thenReturn(List.of(plant));

        List<Plant> result = plantService.getPlantsForMonth(1L, month);

        assertEquals(1, result.size());
    }

    @Test
    void getMonthCalendar_fillsMissingDays() {
        YearMonth month = YearMonth.of(2026, 2);
        Plant plant = new Plant();
        plant.setPlantDate(month.atDay(2));
        plant.setStatus(PlantStatus.NORMAL);
        plant.setType(PlantType.TREE);
        plant.setStage(1);
        plant.setLastInteraction(month.atDay(2));

        when(plantRepository.findAllByUser_UidAndPlantDateBetweenOrderByPlantDateAsc(eq(1L), eq(month.atDay(1)), eq(month.atEndOfMonth())))
                .thenReturn(List.of(plant));

        List<MonthPlantDayDto> out = plantService.getMonthCalendar(1L, month);
        assertEquals(month.lengthOfMonth(), out.size());
        assertNotNull(out.get(1).getPlant()); // 2/2 有 plant
        assertNull(out.get(0).getPlant());
    }

    @Test
    void updatePlant_throwsWhenNotFound() {
        when(plantRepository.findByPidAndUser_Uid(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> plantService.updatePlant(1L, 2L, new PlantUpdateRequest()));
    }

    @Test
    void updatePlant_throwsWhenStageNegative() {
        PlantUpdateRequest req = new PlantUpdateRequest();
        req.setStage(-1);
        when(plantRepository.findByPidAndUser_Uid(2L, 1L)).thenReturn(Optional.of(new Plant()));

        assertThrows(IllegalArgumentException.class, () -> plantService.updatePlant(1L, 2L, req));
    }

    @Test
    void updatePlant_returnsPlantWithoutSaveWhenNoFieldsChanged() {
        Plant plant = new Plant();
        PlantUpdateRequest req = new PlantUpdateRequest();
        when(plantRepository.findByPidAndUser_Uid(2L, 1L)).thenReturn(Optional.of(plant));

        Plant out = plantService.updatePlant(1L, 2L, req);

        assertEquals(plant, out);
        verify(plantRepository, never()).save(any());
    }

    @Test
    void updatePlant_updatesAndSavesWhenFieldsProvided() {
        Plant plant = new Plant();
        PlantUpdateRequest req = new PlantUpdateRequest();
        req.setStage(3);
        req.setStatus(PlantStatus.DEAD);
        req.setType(PlantType.CACTUS);

        when(plantRepository.findByPidAndUser_Uid(2L, 1L)).thenReturn(Optional.of(plant));
        when(plantRepository.save(any(Plant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plant out = plantService.updatePlant(1L, 2L, req);

        assertEquals(3, out.getStage());
        assertEquals(PlantStatus.DEAD, out.getStatus());
        assertEquals(PlantType.CACTUS, out.getType());
    }

    @Test
    void deletePlant_throwsWhenMissing() {
        when(plantRepository.findByPidAndUser_Uid(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> plantService.deletePlant(1L, 2L));
    }

    @Test
    void deletePlant_deletesWhenFound() {
        Plant plant = new Plant();
        when(plantRepository.findByPidAndUser_Uid(2L, 1L)).thenReturn(Optional.of(plant));

        plantService.deletePlant(1L, 2L);

        verify(plantRepository).delete(plant);
    }
}

