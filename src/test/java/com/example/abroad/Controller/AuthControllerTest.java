package com.example.abroad.Controller;

import com.example.abroad.configuration.AuthSuccessHandler;
import com.example.abroad.controller.AuthController;
import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.Student;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

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
    private Model model;

    @Mock
    private Authentication authentication;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userService, authenticationManager, authSuccessHandler);
    }

    @Test
    void showLoginForm_WhenUserNotAuthenticated_ReturnsLoginView() {
        // Arrange
        when(userService.getUser(session)).thenReturn(Optional.empty());

        // Act
        String result = authController.showLoginForm(session, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), model);

        // Assert
        assertEquals("auth/login", result);
        verify(model).addAttribute(eq("alerts"), any());
    }

    @Test
    void showLoginForm_WhenUserAuthenticated_RedirectsToHome() {
        // Arrange
        Student student = new Student("testUser", "password", "test@test.com", "Test User");
        when(userService.getUser(session)).thenReturn(Optional.of(student));

        // Act
        String result = authController.showLoginForm(session, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), model);

        // Assert
        assertEquals("redirect:/?info=You are already logged in", result);
    }

    @Test
    void logout_InvalidatesSessionAndRedirectsToLogin() {
        // Act
        String result = authController.logout(session);

        // Assert
        assertEquals("redirect:/login?info=You have been logged out", result);
        verify(session).invalidate();
    }

    @Test
    void showRegistrationForm_WhenUserNotAuthenticated_ReturnsRegisterView() {
        // Arrange
        when(session.getAttribute("user")).thenReturn(null);

        // Act
        String result = authController.showRegistrationForm(request, session, response);

        // Assert
        assertEquals("auth/register", result);
    }

    @Test
    void showRegistrationForm_WhenUserAuthenticated_RedirectsToHome() {
        // Arrange
        Student student = new Student("testUser", "password", "test@test.com", "Test User");
        when(session.getAttribute("user")).thenReturn(student);

        // Act
        String result = authController.showRegistrationForm(request, session, response);

        // Assert
        assertEquals("redirect:/", result);
    }

    @Test
    void registerUser_SuccessfulRegistration_RedirectsViaAuthHandler() throws IOException {
        // Arrange
        String username = "testuser";
        String password = "password";
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(request.getSession(true)).thenReturn(session);

        // Act
        String result = authController.registerUser(username, "Test User", "test@test.com",
                password, request, response, model);

        // Assert
        assertNull(result); // Because redirect is handled by authSuccessHandler
        verify(userService).registerStudent(username, "Test User", "test@test.com", password);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authSuccessHandler).onAuthenticationSuccess(request, response, authentication);
    }

    @Test
    void registerUser_WhenUsernameAlreadyExists_ReturnsRegisterView() throws IOException {
        // Arrange
        doThrow(new UsernameAlreadyInUseException("Username already exists"))
                .when(userService).registerStudent(any(), any(), any(), any());

        // Act
        String result = authController.registerUser("existinguser", "Test User",
                "test@test.com", "password", request, response, model);

        // Assert
        assertEquals("auth/register", result);
        verify(model).addAttribute("error", "Username is already taken");
    }

    @Test
    void registerUser_WhenEmailAlreadyExists_ReturnsRegisterView() throws IOException {
        // Arrange
        doThrow(new EmailAlreadyInUseException("Email already exists"))
                .when(userService).registerStudent(any(), any(), any(), any());

        // Act
        String result = authController.registerUser("newuser", "Test User",
                "existing@test.com", "password", request, response, model);

        // Assert
        assertEquals("auth/register", result);
        verify(model).addAttribute("error", "Email is already registered");
    }
}