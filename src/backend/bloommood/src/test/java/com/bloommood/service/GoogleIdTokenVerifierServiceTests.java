package com.bloommood.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleIdTokenVerifierServiceTests {

    @Test
    void verify_throwsWhenTokenBlank() {
        GoogleIdTokenVerifierService service = new GoogleIdTokenVerifierService("client-id");

        assertThrows(JwtException.class, () -> service.verify(" "));
    }
}

