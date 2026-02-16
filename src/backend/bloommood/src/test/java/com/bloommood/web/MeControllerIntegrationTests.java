package com.bloommood.web;

import com.bloommood.controller.MeController;
import com.bloommood.dto.UpdateRequest;
import com.bloommood.dto.UserViewDto;
import com.bloommood.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MeControllerIntegrationTests {

    private static Authentication authWithUid(String uid) {
        Authentication a = mock(Authentication.class);
        when(a.getName()).thenReturn(uid);
        return a;
    }

    @Test
    void getInfo_unauthorized_whenNoAuth() {
        UserService userService = mock(UserService.class);
        MeController c = new MeController(userService);

        var resp = c.getInfo(null);
        assertEquals(401, resp.getStatusCode().value());
        verifyNoInteractions(userService);
    }

    @Test
    void getInfo_ok() {
        UserService userService = mock(UserService.class);
        when(userService.getInfo(1L)).thenReturn(new UserViewDto(1L, "a@b.com", "A", "ROLE_USER"));

        MeController c = new MeController(userService);
        var resp = c.getInfo(authWithUid("1"));

        assertEquals(200, resp.getStatusCode().value());
        verify(userService).getInfo(1L);
    }

    @Test
    void updateMe_badRequest_whenServiceThrowsIllegalArgument() {
        UserService userService = mock(UserService.class);
        when(userService.updateMe(eq(1L), any(UpdateRequest.class))).thenThrow(new IllegalArgumentException("x"));

        MeController c = new MeController(userService);
        var resp = c.updateMe(new UpdateRequest(), authWithUid("1"));

        assertEquals(400, resp.getStatusCode().value());
    }
}