package com.bloommood.controller;

import com.bloommood.dto.UserViewDto;
import com.bloommood.entity.User;
import com.bloommood.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/debug")
public class DebugUserController {

    private final UserRepository userRepository;

    public DebugUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<UserViewDto> listUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(u -> new UserViewDto(u.getUid(), u.getEmail(), u.getUname(), u.getRole()))
                .toList();
    }
}
