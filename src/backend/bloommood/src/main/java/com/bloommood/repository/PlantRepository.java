package com.bloommood.repository;

import com.bloommood.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Repository for Plant entity, providing CRUD operations and custom queries.
public interface PlantRepository extends JpaRepository<Plant, Long> {

    Optional<Plant> findByUser_UidAndPlantDate(Long uid, LocalDate plantDate);

    boolean existsByUser_UidAndPlantDate(Long uid, LocalDate plantDate);

    List<Plant> findAllByUser_UidAndPlantDateBetweenOrderByPlantDateAsc(Long uid, LocalDate startInclusive, LocalDate endInclusive);

    Optional<Plant> findByPidAndUser_Uid(Long pid, Long uid);

    Optional<Plant> findTopByUser_UidOrderByPlantDateDescPidDesc(Long uid);

    void deleteByPidAndUser_Uid(Long pid, Long uid);
}
