package com.bloommood.web;

import com.bloommood.controller.ActionController;
import com.bloommood.dto.ActionCreateRequest;
import com.bloommood.entity.Action;
import com.bloommood.entity.ActionType;
import com.bloommood.service.ActionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionControllerIntegrationTests {

    private static Authentication authWithUid(String uid) {
        Authentication a = mock(Authentication.class);
        when(a.getName()).thenReturn(uid);
        return a;
    }

    @Test
    void create_unauthorized_whenNoAuth() {
        ActionService actionService = mock(ActionService.class);
        ActionController c = new ActionController(actionService);

        var resp = c.create(new ActionCreateRequest(), null);
        assertEquals(401, resp.getStatusCode().value());
        verifyNoInteractions(actionService);
    }

    @Test
    void month_invalidYm_400() {
        ActionService actionService = mock(ActionService.class);
        ActionController c = new ActionController(actionService);

        var resp = c.getMonth("bad", authWithUid("1"));
        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void create_ok() {
        ActionService actionService = mock(ActionService.class);
        ActionController c = new ActionController(actionService);

        ActionCreateRequest req = new ActionCreateRequest();
        req.setType(ActionType.WATER);

        Action a = mock(Action.class);
        when(a.getId()).thenReturn(1L);
        when(a.getType()).thenReturn(ActionType.WATER);
        when(a.getActionTime()).thenReturn(LocalDateTime.now());
        when(actionService.createAction(1L, ActionType.WATER)).thenReturn(a);

        var resp = c.create(req, authWithUid("1"));
        assertEquals(200, resp.getStatusCode().value());
        verify(actionService).createAction(1L, ActionType.WATER);
    }
}