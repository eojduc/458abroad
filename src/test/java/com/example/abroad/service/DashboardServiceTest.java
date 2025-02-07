package com.example.abroad.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.abroad.model.Admin;
import com.example.abroad.model.Student;
import com.example.abroad.service.DashboardService.GetDashboard;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpSession session;

    private DashboardService dashboardService;

    private final Student testStudent = new Student(
            "student1",
            "hashedpass123",
            "student@test.com",
            "Test Student"
    );

    private final Admin testAdmin = new Admin(
            "admin1",
            "hashedpass123",
            "admin@test.com",
            "Test Admin"
    );

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(userService);
    }

    @Test
    void getDashboard_WhenNotLoggedIn_ReturnsNotLoggedIn() {
        when(userService.getUser(session)).thenReturn(Optional.empty());

        var result = dashboardService.getDashboard(session);

        assertTrue(result instanceof GetDashboard.NotLoggedIn);
    }

    @Test
    void getDashboard_WhenLoggedInAsStudent_ReturnsStudentDashboard() {
        when(userService.getUser(session)).thenReturn(Optional.of(testStudent));

        var result = dashboardService.getDashboard(session);

        assertTrue(result instanceof GetDashboard.StudentDashboard);
        var dashboard = (GetDashboard.StudentDashboard) result;
        assertEquals(testStudent, dashboard.user());
    }

    @Test
    void getDashboard_WhenLoggedInAsAdmin_ReturnsAdminDashboard() {
        when(userService.getUser(session)).thenReturn(Optional.of(testAdmin));

        var result = dashboardService.getDashboard(session);

        assertTrue(result instanceof GetDashboard.AdminDashboard);
        var dashboard = (GetDashboard.AdminDashboard) result;
        assertEquals(testAdmin, dashboard.user());
    }

    @Test
    void setTheme_DelegatesCallToUserService() {
        String theme = "dark";

        dashboardService.setTheme(theme, session);

        verify(userService).setTheme(theme, session);
    }

    @Test
    void getTheme_DelegatesCallToUserService() {
        String expectedTheme = "light";
        when(userService.getTheme(session)).thenReturn(expectedTheme);

        String result = dashboardService.getTheme(session);

        assertEquals(expectedTheme, result);
        verify(userService).getTheme(session);
    }
}
