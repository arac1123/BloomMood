    package com.bloommood.service;

    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.security.oauth2.jwt.Jwt;
    import org.springframework.security.oauth2.jwt.JwtDecoder;
    import org.springframework.security.oauth2.jwt.JwtException;
    import org.springframework.security.oauth2.jwt.JwtValidators;
    import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
    import org.springframework.stereotype.Service;

    import java.util.Map;

    /**
     * Verifies Google ID Token (JWT) received from Google Identity Services.
     *
     * Validates:
     * - Signature (via Google's JWKs)
     * - Issuer (iss)
     * - Expiry (exp)
     * - Audience (aud contains configured app.google.client-id)
     */
    @Service
    public class GoogleIdTokenVerifierService {

        private static final String GOOGLE_JWKS = "https://www.googleapis.com/oauth2/v3/certs";
        private static final String GOOGLE_ISSUER = "https://accounts.google.com";

        private final JwtDecoder jwtDecoder;
        private final String clientId;

        public GoogleIdTokenVerifierService(@Value("${app.google.client-id:}") String clientId) {
            this.clientId = clientId;

            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS).build();
            // issuer + time validation
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(GOOGLE_ISSUER));
            this.jwtDecoder = decoder;
        }

        /**
         * @return Google token claims. Must include: sub, email, name (if provided)
         * @throws JwtException if token invalid or audience mismatch
         */
        public Map<String, Object> verify(String idToken) {
            if (idToken == null || idToken.isBlank()) {
                throw new JwtException("idToken is blank");
            }

            Jwt jwt = jwtDecoder.decode(idToken);

            if (clientId == null || clientId.isBlank()) {
                throw new JwtException("google client id not configured");
            }

            if (!jwt.getAudience().contains(clientId)) {
                throw new JwtException("invalid audience");
            }

            return jwt.getClaims();
        }
    }

