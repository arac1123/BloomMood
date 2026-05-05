package com.bloommood.web;

import com.bloommood.controller.DebugUserController;
import com.bloommood.entity.User;
import com.bloommood.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DebugUserControllerIntegrationTests {

    @Test
    void listUsers_returnsDtos() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findAll()).thenReturn(List.of(new User("a@b.com", "A", "hash", "ROLE_USER")));

        DebugUserController c = new DebugUserController(repo);
        var list = c.listUsers();
        assertEquals(1, list.size());
        assertEquals("a@b.com", list.get(0).getEmail());
    }
}