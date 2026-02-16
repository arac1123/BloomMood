package com.bloommood.web;

import com.bloommood.controller.AuthController;
import com.bloommood.dto.GoogleAuthCodeRequest;
import com.bloommood.dto.LoginRequest;
import com.bloommood.dto.RegisterRequest;
import com.bloommood.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerIntegrationTests {

    @Test
    void register_validation_400() {
        AuthService authService = mock(AuthService.class);
        AuthController c = new AuthController(authService);

        RegisterRequest req = new RegisterRequest();
        var resp = c.register(req);

        assertEquals(400, resp.getStatusCode().value());
        verifyNoInteractions(authService);
    }

    @Test
    void login_ok_callsService() {
        AuthService authService = mock(AuthService.class);
        AuthController c = new AuthController(authService);

        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.com");
        req.setPassword("pw");

        HttpServletResponse response = mock(HttpServletResponse.class);
        var resp = c.login(req, response);

        assertEquals(200, resp.getStatusCode().value());
        verify(authService).loginLocal("a@b.com", "pw", response);
    }

    @Test
    void google_missingCode_400() {
        AuthService authService = mock(AuthService.class);
        AuthController c = new AuthController(authService);

        GoogleAuthCodeRequest req = new GoogleAuthCodeRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        var resp = c.google(req, response);

        assertEquals(400, resp.getStatusCode().value());
        verifyNoInteractions(authService);
    }
}