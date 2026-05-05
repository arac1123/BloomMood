package com.bloommood.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Exchanges Google OAuth authorization code for tokens at Google's token endpoint.
 *
 * Teaching version (no PKCE, no refresh_token persistence):
 * - Input: code
 * - Output: token response containing id_token (and access_token, expires_in, ...)
 */
@Service
public class GoogleOAuthTokenService {

    private final RestClient restClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOAuthTokenService(
            @Value("${app.google.token-uri:https://oauth2.googleapis.com/token}") String tokenUri,
            @Value("${app.google.client-id:}") String clientId,
            @Value("${app.google.client-secret:}") String clientSecret,
            @Value("${app.google.redirect-uri:}") String redirectUri
    ) {
        this.restClient = RestClient.create();
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    /**
     * @return Raw token response JSON as Map. Contains id_token on success.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> exchangeCodeForTokens(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code is blank");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("app.google.client-id not configured");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("app.google.client-secret not configured");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalStateException("app.google.redirect-uri not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("grant_type", "authorization_code");
        form.add("redirect_uri", redirectUri);

        return (Map<String, Object>) restClient
                .post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);
    }
}
