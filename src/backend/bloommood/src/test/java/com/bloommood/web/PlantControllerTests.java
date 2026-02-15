package com.bloommood.web;

import com.bloommood.dto.PlantUpdateRequest;
import com.bloommood.entity.Plant;
import com.bloommood.entity.PlantStatus;
import com.bloommood.entity.PlantType;
import com.bloommood.entity.User;
import com.bloommood.repository.PlantRepository;
import com.bloommood.repository.UserRepository;
import com.bloommood.service.PlantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PlantControllerTests {

    @Autowired
    PlantService plantService;

    @Autowired
    PlantRepository plantRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void updateAndDeleteRequireOwnership() {
        // create users
        User u1 = userRepository.save(new User("u1@t.com", "U1", "hash", "ROLE_USER"));
        User u2 = userRepository.save(new User("u2@t.com", "U2", "hash", "ROLE_USER"));

        Plant p1 = plantRepository.save(new Plant(u1, LocalDate.now(), PlantType.FLOWER));
        Plant p2 = plantRepository.save(new Plant(u2, LocalDate.now(), PlantType.TREE));

        // update own
        PlantUpdateRequest req = new PlantUpdateRequest();
        req.setStage(3);
        req.setStatus(PlantStatus.WITHERED);
        Plant updated = plantService.updatePlant(u1.getUid(), p1.getPid(), req);
        assertEquals(3, updated.getStage());
        assertEquals(PlantStatus.WITHERED, updated.getStatus());

        // update other's should fail
        assertThrows(IllegalArgumentException.class, () -> plantService.updatePlant(u1.getUid(), p2.getPid(), req));

        // delete own
        plantService.deletePlant(u1.getUid(), p1.getPid());
        assertTrue(plantRepository.findById(p1.getPid()).isEmpty());
    }
}
