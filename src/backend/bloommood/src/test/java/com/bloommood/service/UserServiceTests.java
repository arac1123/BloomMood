package com.bloommood.service;

import com.bloommood.dto.UpdateRequest;
import com.bloommood.entity.User;
import com.bloommood.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTests {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void updateMe_changesUname() {
        User u = new User("usvc@test.com", "Old", passwordEncoder.encode("12345678"), "ROLE_USER");
        u = userRepository.save(u);

        UpdateRequest req = new UpdateRequest();
        req.setUname("NewName");

        boolean changed = userService.updateMe(u.getUid(), req);
        assertTrue(changed);

        User updated = userRepository.findById(u.getUid()).orElseThrow();
        assertEquals("NewName", updated.getUname());
    }

    @Test
    void updateMe_changePassword_requiresOldPassword() {
        User u0 = new User("usvc2@test.com", "U", passwordEncoder.encode("oldpw"), "ROLE_USER");
        User u = userRepository.save(u0);
        final Long uid = u.getUid();

        UpdateRequest req = new UpdateRequest();
        req.setNewPassword("newpw");

        assertThrows(IllegalArgumentException.class, () -> userService.updateMe(uid, req));
    }
}