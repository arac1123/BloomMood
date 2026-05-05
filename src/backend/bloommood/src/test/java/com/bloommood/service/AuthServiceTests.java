package com.bloommood.service;

import com.bloommood.entity.AuthProvider;
import com.bloommood.entity.User;
import com.bloommood.repository.UserRepository;
import com.bloommood.security.AuthCookieUtil;
import com.bloommood.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    private static final String COOKIE_NAME = "accessToken";
    private static final int COOKIE_MAX_AGE = 3600;
    private static final boolean COOKIE_SECURE = false;
    private static final String COOKIE_SAME_SITE = "Lax";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GoogleIdTokenVerifierService googleVerifier;

    @Mock
    private GoogleOAuthTokenService googleOAuthTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtUtil,
                googleVerifier,
                googleOAuthTokenService,
                COOKIE_NAME,
                COOKIE_MAX_AGE,
                COOKIE_SECURE,
                COOKIE_SAME_SITE
        );
    }

    @Test
    void registerLocal_success_normalizesEmailAndSavesUser() {
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pw123")).thenReturn("hashed");

        authService.registerLocal("  Demo@Example.COM  ", "  DemoUser  ", "pw123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).existsByEmail("demo@example.com");
        verify(passwordEncoder).encode("pw123");
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("demo@example.com", saved.getEmail());
        assertEquals("DemoUser", saved.getUname());
        assertEquals("hashed", saved.getPassword());
        assertEquals(AuthProvider.LOCAL, saved.getAuthProvider());
    }

    @Test
    void registerLocal_emailExists_throwsAndDoesNotSave() {
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.registerLocal("demo@example.com", "demo", "pw")
        );

        assertEquals("email already exists", ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginLocal_success_issuesJwtCookie() {
        User user = new User("u@test.com", "U", "hashed", "ROLE_USER");
        ReflectionTestUtils.setField(user, "uid", 7L);

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPw", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("7", "ROLE_USER")).thenReturn("jwt-token");

        HttpServletResponse response = mock(HttpServletResponse.class);
        try (MockedStatic<AuthCookieUtil> cookieUtil = mockStatic(AuthCookieUtil.class)) {
            authService.loginLocal("  U@TEST.com  ", "plainPw", response);

            verify(userRepository).findByEmail("u@test.com");
            verify(passwordEncoder).matches("plainPw", "hashed");
            verify(jwtUtil).generateToken("7", "ROLE_USER");
            cookieUtil.verify(() -> AuthCookieUtil.setJwtCookie(
                    response,
                    COOKIE_NAME,
                    "jwt-token",
                    COOKIE_MAX_AGE,
                    COOKIE_SECURE,
                    COOKIE_SAME_SITE
            ));
        }
    }

    @Test
    void loginLocal_userNotFound_throwsInvalidCredentials() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.loginLocal("missing@test.com", "pw", mock(HttpServletResponse.class))
        );

        assertEquals("invalid credentials", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void loginLocal_passwordMismatch_throwsInvalidCredentials() {
        User user = new User("u@test.com", "U", "hashed", "ROLE_USER");
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.loginLocal("u@test.com", "wrong", mock(HttpServletResponse.class))
        );

        assertEquals("invalid credentials", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void loginGoogleByCode_missingIdToken_throws() {
        when(googleOAuthTokenService.exchangeCodeForTokens("code-1")).thenReturn(Map.of("access_token", "x"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.loginGoogleByCode("code-1", mock(HttpServletResponse.class))
        );

        assertEquals("missing id_token from google", ex.getMessage());
    }

    @Test
    void loginGoogleByCode_missingSub_throws() {
        when(googleOAuthTokenService.exchangeCodeForTokens("code-2")).thenReturn(Map.of("id_token", "id-token"));
        when(googleVerifier.verify("id-token")).thenReturn(Map.of("email", "g@test.com", "name", "G"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.loginGoogleByCode("code-2", mock(HttpServletResponse.class))
        );

        assertEquals("invalid google token", ex.getMessage());
    }

    @Test
    void loginGoogleByCode_missingEmail_throws() {
        when(googleOAuthTokenService.exchangeCodeForTokens("code-3")).thenReturn(Map.of("id_token", "id-token"));
        when(googleVerifier.verify("id-token")).thenReturn(Map.of("sub", "google-sub", "name", "G"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.loginGoogleByCode("code-3", mock(HttpServletResponse.class))
        );

        assertEquals("google email missing", ex.getMessage());
    }

    @Test
    void loginGoogleByCode_existingOauthUser_issuesJwtCookie() {
        User existing = new User("g@test.com", "G", null, "ROLE_USER", AuthProvider.GOOGLE, "sub-1");
        ReflectionTestUtils.setField(existing, "uid", 99L);

        when(googleOAuthTokenService.exchangeCodeForTokens("code-4")).thenReturn(Map.of("id_token", "id-token"));
        when(googleVerifier.verify("id-token")).thenReturn(Map.of("sub", "sub-1", "email", "g@test.com", "name", "G"));
        when(userRepository.findByOauthSub("sub-1")).thenReturn(Optional.of(existing));
        when(jwtUtil.generateToken("99", "ROLE_USER")).thenReturn("jwt-google-existing");

        HttpServletResponse response = mock(HttpServletResponse.class);
        try (MockedStatic<AuthCookieUtil> cookieUtil = mockStatic(AuthCookieUtil.class)) {
            authService.loginGoogleByCode("code-4", response);

            verify(userRepository, never()).save(any(User.class));
            cookieUtil.verify(() -> AuthCookieUtil.setJwtCookie(
                    response,
                    COOKIE_NAME,
                    "jwt-google-existing",
                    COOKIE_MAX_AGE,
                    COOKIE_SECURE,
                    COOKIE_SAME_SITE
            ));
        }
    }

    @Test
    void loginGoogleByCode_newUserEmailExists_throws() {
        when(googleOAuthTokenService.exchangeCodeForTokens("code-5")).thenReturn(Map.of("id_token", "id-token"));
        when(googleVerifier.verify("id-token")).thenReturn(Map.of("sub", "sub-2", "email", "dup@test.com", "name", "Dup"));
        when(userRepository.findByOauthSub("sub-2")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.loginGoogleByCode("code-5", mock(HttpServletResponse.class))
        );

        assertEquals("email already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginGoogleByCode_newGoogleUser_savesAndIssuesJwtCookie() {
        when(googleOAuthTokenService.exchangeCodeForTokens("code-6")).thenReturn(Map.of("id_token", "id-token"));
        when(googleVerifier.verify("id-token")).thenReturn(Map.of(
                "sub", "sub-3",
                "email", "  NewGoogle@Example.com  ",
                "name", "  New Google User  "
        ));
        when(userRepository.findByOauthSub("sub-3")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("newgoogle@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            ReflectionTestUtils.setField(u, "uid", 123L);
            return u;
        });
        when(jwtUtil.generateToken("123", "ROLE_USER")).thenReturn("jwt-google-new");

        HttpServletResponse response = mock(HttpServletResponse.class);
        try (MockedStatic<AuthCookieUtil> cookieUtil = mockStatic(AuthCookieUtil.class)) {
            authService.loginGoogleByCode("code-6", response);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).existsByEmail("newgoogle@example.com");
            verify(userRepository).save(userCaptor.capture());
            verify(jwtUtil).generateToken("123", "ROLE_USER");
            User saved = userCaptor.getValue();
            assertEquals("newgoogle@example.com", saved.getEmail());
            assertEquals("New Google User", saved.getUname());
            assertEquals(AuthProvider.GOOGLE, saved.getAuthProvider());
            assertEquals("sub-3", saved.getOauthSub());
            cookieUtil.verify(() -> AuthCookieUtil.setJwtCookie(
                    response,
                    COOKIE_NAME,
                    "jwt-google-new",
                    COOKIE_MAX_AGE,
                    COOKIE_SECURE,
                    COOKIE_SAME_SITE
            ));
        }
    }

    @Test
    void logout_clearsJwtCookie() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        try (MockedStatic<AuthCookieUtil> cookieUtil = mockStatic(AuthCookieUtil.class)) {
            authService.logout(response);

            cookieUtil.verify(() -> AuthCookieUtil.clearJwtCookie(
                    response,
                    COOKIE_NAME,
                    COOKIE_SECURE,
                    COOKIE_SAME_SITE
            ));
        }
    }
}


