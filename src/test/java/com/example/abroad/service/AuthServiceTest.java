package com.example.abroad.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.abroad.configuration.AuthSuccessHandler;
import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.Student;
import com.example.abroad.service.AuthService.CheckLoginStatus;
import com.example.abroad.service.AuthService.RegisterResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthSuccessHandler authSuccessHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    private AuthService authService;

    private final Student testStudent = new Student(
            "testuser",
            "hashedpass123",
            "test@example.com",
            "Test User"
    );

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userService,
                authenticationManager,
                authSuccessHandler
        );
    }

    @Test
    void checkLoginStatus_WhenUserLoggedIn_ReturnsAlreadyLoggedIn() {
        when(userService.getUser(session)).thenReturn(Optional.of(testStudent));

        var result = authService.checkLoginStatus(session);

        assertTrue(result instanceof CheckLoginStatus.AlreadyLoggedIn);
    }

    @Test
    void checkLoginStatus_WhenUserNotLoggedIn_ReturnsNotLoggedIn() {
        when(userService.getUser(session)).thenReturn(Optional.empty());

        var result = authService.checkLoginStatus(session);

        assertTrue(result instanceof CheckLoginStatus.NotLoggedIn);
    }

    @Test
    void registerUser_WhenSuccessful_ReturnsSuccess() {
        // Setup
        String username = "newuser";
        String displayName = "New User";
        String email = "new@example.com";
        String password = "password123";

        when(request.getSession(true)).thenReturn(session);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Execute
        var result = authService.registerUser(username, displayName, email, password, request);

        // Verify
        assertTrue(result instanceof RegisterResult.Success);
        verify(userService).registerStudent(username, displayName, email, password);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(session).setAttribute(any(), any());
    }

    @Test
    void registerUser_WhenUsernameExists_ReturnsUsernameExists() {
        // Setup
        String username = "existinguser";
        String displayName = "Existing User";
        String email = "existing@example.com";
        String password = "password123";

        doThrow(new UsernameAlreadyInUseException("username in use"))
                .when(userService)
                .registerStudent(username, displayName, email, password);

        // Execute
        var result = authService.registerUser(username, displayName, email, password, request);

        // Verify
        assertTrue(result instanceof RegisterResult.UsernameExists);
        verify(userService).registerStudent(username, displayName, email, password);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void registerUser_WhenEmailExists_ReturnsEmailExists() {
        // Setup
        String username = "newuser";
        String displayName = "New User";
        String email = "existing@example.com";
        String password = "password123";

        doThrow(new EmailAlreadyInUseException("username in use"))
                .when(userService)
                .registerStudent(username, displayName, email, password);

        // Execute
        var result = authService.registerUser(username, displayName, email, password, request);

        // Verify
        assertTrue(result instanceof RegisterResult.EmailExists);
        verify(userService).registerStudent(username, displayName, email, password);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void registerUser_WhenAuthenticationFails_ReturnsAuthenticationError() {
        // Setup
        String username = "newuser";
        String displayName = "New User";
        String email = "new@example.com";
        String password = "password123";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Execute
        var result = authService.registerUser(username, displayName, email, password, request);

        // Verify
        assertTrue(result instanceof RegisterResult.AuthenticationError);
        verify(userService).registerStudent(username, displayName, email, password);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void handleSuccessfulAuthentication_CallsAuthSuccessHandler() throws IOException {
        // Execute
        authService.handleSuccessfulAuthentication(request, response, authentication);

        // Verify
        verify(authSuccessHandler).onAuthenticationSuccess(request, response, authentication);
    }

    @Test
    void handleSuccessfulAuthentication_PropagatesIOException() throws IOException {
        // Setup
        doThrow(new IOException())
                .when(authSuccessHandler)
                .onAuthenticationSuccess(request, response, authentication);

        // Execute & Verify
        assertThrows(IOException.class, () ->
                authService.handleSuccessfulAuthentication(request, response, authentication)
        );
    }
}