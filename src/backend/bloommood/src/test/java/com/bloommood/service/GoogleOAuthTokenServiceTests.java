package com.bloommood.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleOAuthTokenServiceTests {

    @Test
    void exchangeCodeForTokens_throwsWhenCodeBlank() {
        GoogleOAuthTokenService service = new GoogleOAuthTokenService(
                "https://oauth2.googleapis.com/token",
                "client-id",
                "client-secret",
                "http://localhost/callback"
        );

        assertThrows(IllegalArgumentException.class, () -> service.exchangeCodeForTokens(" "));
    }

    @Test
    void exchangeCodeForTokens_throwsWhenClientIdMissing() {
        GoogleOAuthTokenService service = new GoogleOAuthTokenService(
                "https://oauth2.googleapis.com/token",
                "",
                "client-secret",
                "http://localhost/callback"
        );

        assertThrows(IllegalStateException.class, () -> service.exchangeCodeForTokens("auth-code"));
    }

    @Test
    void exchangeCodeForTokens_throwsWhenClientSecretMissing() {
        GoogleOAuthTokenService service = new GoogleOAuthTokenService(
                "https://oauth2.googleapis.com/token",
                "client-id",
                "",
                "http://localhost/callback"
        );

        assertThrows(IllegalStateException.class, () -> service.exchangeCodeForTokens("auth-code"));
    }

    @Test
    void exchangeCodeForTokens_throwsWhenRedirectUriMissing() {
        GoogleOAuthTokenService service = new GoogleOAuthTokenService(
                "https://oauth2.googleapis.com/token",
                "client-id",
                "client-secret",
                ""
        );

        assertThrows(IllegalStateException.class, () -> service.exchangeCodeForTokens("auth-code"));
    }
}

