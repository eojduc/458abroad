/*
package com.example.abroad.Controller;

import com.example.abroad.controller.DashboardController;
import com.example.abroad.model.Admin;
import com.example.abroad.model.Alerts;
import com.example.abroad.model.Student;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private FormatService formatService;

    @Mock
    private UserService userService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        dashboardController = new DashboardController(formatService, userService);
    }

    @Test
    void home_WhenUserNotLoggedIn_ReturnsHomepage() {
        // Arrange
        when(userService.getUser(session)).thenReturn(Optional.empty());
        when(userService.getTheme(session)).thenReturn("light");

        // Act
        String result = dashboardController.home(session, model, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        // Assert
        assertEquals("homepage", result);
        verify(model).addAttribute("theme", "light");
    }


    @Test
    void hello_WhenAuthenticated_SetsNameAttribute() {
        // Arrange
        when(authentication.getName()).thenReturn("testUser");

        // Act
        String result = dashboardController.hello(authentication, model);

        // Assert
        assertEquals("hello", result);
        verify(model).addAttribute("name", "testUser");
    }

    @Test
    void hello_WhenNotAuthenticated_DoesNotSetNameAttribute() {
        // Act
        String result = dashboardController.hello(null, model);

        // Assert
        assertEquals("hello", result);
        verify(model, never()).addAttribute(eq("name"), any());
    }


    @Test
    void testAuth_WhenNotAuthenticated_DoesNotSetAttributes() {
        // Act
        String result = dashboardController.testAuth(null, model);

        // Assert
        assertEquals("test-auth", result);
        verify(model, never()).addAttribute(eq("username"), any());
        verify(model, never()).addAttribute(eq("roles"), any());
    }

    @Test
    void setTheme_UpdatesThemeAndRedirects() {
        // Arrange
        String theme = "dark";

        // Act
        String result = dashboardController.setTheme(theme, session);

        // Assert
        assertEquals("redirect:/", result);
        verify(userService).setTheme(theme, session);
    }

    @Test
    void showDashboard_SetsCorrectAttributesForStudent() {
        // Arrange
        Student student = new Student("testStudent", "password", "student@test.com", "Test Student");

        // Act
        String result = dashboardController.showDashboard(model, student);

        // Assert
        assertEquals("student/student-dashboard :: page", result);
        verify(model).addAttribute("displayName", "Test Student");
        verify(model).addAttribute("student", "testStudent");
    }

    @Test
    void adminDashboard_SetsCorrectAttributesForAdmin() {
        // Arrange
        Admin admin = new Admin("testAdmin", "password", "admin@test.com", "Test Admin");

        // Act
        String result = dashboardController.adminDashboard(model, admin);

        // Assert
        assertEquals("admin/admin-dashboard :: page", result);
        verify(model).addAttribute("displayName", "Test Admin");
    }
}
 */