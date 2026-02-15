package com.bloommood.repository;

import com.bloommood.entity.AuthProvider;
import com.bloommood.entity.Plant;
import com.bloommood.entity.PlantType;
import com.bloommood.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PlantRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Test
    void findAllByUserAndMonth_rangeQueryWorks() {
        User user = new User("u1@test.com", "U1", "hash", "ROLE_USER");
        user.setAuthProvider(AuthProvider.LOCAL);
        user = userRepository.save(user);

        LocalDate d1 = LocalDate.of(2026, 2, 1);
        LocalDate d2 = LocalDate.of(2026, 2, 15);
        LocalDate d3 = LocalDate.of(2026, 3, 1);

        plantRepository.save(new Plant(user, d1, PlantType.FLOWER));
        plantRepository.save(new Plant(user, d2, PlantType.CACTUS));
        plantRepository.save(new Plant(user, d3, PlantType.TREE));

        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 28);

        List<Plant> feb = plantRepository.findAllByUser_UidAndPlantDateBetweenOrderByPlantDateAsc(user.getUid(), start, end);
        assertEquals(2, feb.size());
        assertEquals(d1, feb.get(0).getPlantDate());
        assertEquals(d2, feb.get(1).getPlantDate());
    }
}
